package com.naoesqueci.app.presentation.ui.screen.item_edit

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.naoesqueci.app.domain.model.TripItem
import com.naoesqueci.app.domain.usecase.item.AddItemUseCase
import com.naoesqueci.app.domain.usecase.item.DeleteItemUseCase
import com.naoesqueci.app.domain.usecase.item.GetItemNameSuggestionsUseCase
import com.naoesqueci.app.domain.usecase.item.GetItemsUseCase
import com.naoesqueci.app.domain.usecase.item.UpdateItemUseCase
import com.naoesqueci.app.testdoubles.FakeTripItemRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class ItemEditViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var itemRepository: FakeTripItemRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        itemRepository = FakeTripItemRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(tripId: Long = 1L, itemId: Long? = null): ItemEditViewModel {
        return ItemEditViewModel(
            addItemUseCase = AddItemUseCase(itemRepository),
            updateItemUseCase = UpdateItemUseCase(itemRepository),
            deleteItemUseCase = DeleteItemUseCase(itemRepository),
            getItemUseCase = { id -> itemRepository.getItemById(id).first() },
            getItemsUseCase = GetItemsUseCase(itemRepository),
            suggestNamesUseCase = GetItemNameSuggestionsUseCase(itemRepository),
            tripId = tripId,
            itemId = itemId
        )
    }

    @Test
    fun `duplicado com acento e caixa diferente e bloqueado`() = runTest {
        itemRepository.addItem(TripItem(tripId = 1L, name = "Camisa"))

        val vm = viewModel()
        var saved = false
        vm.setName("cãmísa ")
        vm.saveItem { saved = true }
        advanceUntilIdle()

        assertThat(saved).isFalse()
        assertThat(vm.uiState.value.error).isEqualTo("Já existe um item com esse nome")
        assertThat(itemRepository.getItemsByTripId(1L).first()).hasSize(1)
    }

    @Test
    fun `nome e normalizado com primeira letra maiuscula`() = runTest {
        val vm = viewModel()
        var saved = false
        vm.setName("  carregador ")
        vm.saveItem { saved = true }
        advanceUntilIdle()

        assertThat(saved).isTrue()
        assertThat(itemRepository.getItemsByTripId(1L).first().single().name)
            .isEqualTo("Carregador")
    }

    @Test
    fun `editar o proprio item nao conta como duplicado`() = runTest {
        val id = itemRepository.addItem(TripItem(tripId = 1L, name = "Meia"))

        val vm = viewModel(itemId = id)
        advanceUntilIdle()
        var saved = false
        vm.setName("Meia")
        vm.saveItem { saved = true }
        advanceUntilIdle()

        assertThat(saved).isTrue()
        assertThat(vm.uiState.value.error).isNull()
    }

    @Test
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun `sugestoes filtram pelo digitado e excluem igualdade exata`() = runTest {
        itemRepository.addItem(TripItem(tripId = 2L, name = "Camisa"))
        itemRepository.addItem(TripItem(tripId = 2L, name = "Carregador"))
        itemRepository.addItem(TripItem(tripId = 2L, name = "Meia"))

        val vm = viewModel()
        vm.suggestions.test {
            assertThat(awaitItem()).isEmpty()

            vm.setName("Ca")
            advanceUntilIdle()
            val result = awaitItem()
            assertThat(result).containsExactly("Camisa", "Carregador").inOrder()

            vm.setName("Camisa")
            advanceUntilIdle()
            assertThat(awaitItem()).isEmpty()
        }
    }
}
