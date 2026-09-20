package com.naoesqueci.app.presentation.ui.screen.trip_create

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.naoesqueci.app.alarm.AlarmScheduler
import com.naoesqueci.app.domain.usecase.notification.CancelTripNotificationsUseCase
import com.naoesqueci.app.domain.usecase.notification.ScheduleTripNotificationsUseCase
import com.naoesqueci.app.domain.usecase.trip.CreateTripUseCase
import com.naoesqueci.app.domain.usecase.trip.GetTripUseCase
import com.naoesqueci.app.domain.usecase.trip.UpdateTripUseCase
import com.naoesqueci.app.testdoubles.FakeSettingsRepository
import com.naoesqueci.app.testdoubles.FakeTripRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class TripCreateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var tripRepository: FakeTripRepository
    private lateinit var settingsRepository: FakeSettingsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        tripRepository = FakeTripRepository()
        settingsRepository = FakeSettingsRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(tripId: Long? = null): TripCreateViewModel {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val scheduler = AlarmScheduler(context, settingsRepository)
        return TripCreateViewModel(
            createTripUseCase = CreateTripUseCase(tripRepository),
            updateTripUseCase = UpdateTripUseCase(tripRepository),
            getTripUseCase = GetTripUseCase(tripRepository),
            scheduleNotificationsUseCase = ScheduleTripNotificationsUseCase(scheduler),
            cancelNotificationsUseCase = CancelTripNotificationsUseCase(scheduler),
            tripId = tripId
        )
    }

    private fun millisAt(dayOffset: Int, hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, dayOffset)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun `nome vazio bloqueia salvamento`() = runTest {
        val vm = viewModel()
        var saved = false

        vm.setName("   ")
        vm.saveTrip { saved = true }
        advanceUntilIdle()

        assertThat(saved).isFalse()
        assertThat(vm.uiState.value.error).isEqualTo("Digite um nome para a viagem")
        assertThat(tripRepository.getAllTrips().first()).isEmpty()
    }

    @Test
    fun `volta antes da ida bloqueia salvamento`() = runTest {
        val vm = viewModel()
        var saved = false

        vm.setName("Praia")
        vm.setDepartureDate(millisAt(2, 10, 0))
        vm.setDepartureTime(10, 0)
        vm.setReturnDate(millisAt(1, 10, 0))
        vm.setReturnTime(10, 0)
        vm.saveTrip { saved = true }
        advanceUntilIdle()

        assertThat(saved).isFalse()
        assertThat(vm.uiState.value.error).isEqualTo("A volta deve ser depois da ida")
        assertThat(tripRepository.getAllTrips().first()).isEmpty()
    }

    @Test
    fun `salvamento combina data e hora escolhidas`() = runTest {
        val vm = viewModel()
        var saved = false

        vm.setName("Serra")
        vm.setDepartureDate(millisAt(2, 0, 0))
        vm.setDepartureTime(8, 30)
        vm.setReturnDate(millisAt(4, 0, 0))
        vm.setReturnTime(18, 15)
        vm.saveTrip { saved = true }
        advanceUntilIdle()

        assertThat(saved).isTrue()
        val trips = tripRepository.getAllTrips().first()
        assertThat(trips).hasSize(1)

        val departure = Calendar.getInstance().apply { timeInMillis = trips[0].departureDateTime }
        assertThat(departure.get(Calendar.HOUR_OF_DAY)).isEqualTo(8)
        assertThat(departure.get(Calendar.MINUTE)).isEqualTo(30)

        val ret = Calendar.getInstance().apply { timeInMillis = trips[0].returnDateTime }
        assertThat(ret.get(Calendar.HOUR_OF_DAY)).isEqualTo(18)
        assertThat(ret.get(Calendar.MINUTE)).isEqualTo(15)
        assertThat(trips[0].returnDateTime).isGreaterThan(trips[0].departureDateTime)
    }
}
