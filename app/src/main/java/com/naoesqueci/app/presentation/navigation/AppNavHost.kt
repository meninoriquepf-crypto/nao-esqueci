package com.naoesqueci.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.naoesqueci.app.alarm.AlarmScheduler
import com.naoesqueci.app.data.local.database.AppDatabase
import com.naoesqueci.app.data.local.preferences.SettingsDataStore
import com.naoesqueci.app.data.local.repository.CheckStateRepositoryImpl
import com.naoesqueci.app.data.local.repository.NotificationEventRepositoryImpl
import com.naoesqueci.app.data.local.repository.SettingsRepositoryImpl
import com.naoesqueci.app.data.local.repository.TripItemRepositoryImpl
import com.naoesqueci.app.data.local.repository.TripRepositoryImpl
import com.naoesqueci.app.domain.usecase.item.AddItemUseCase
import com.naoesqueci.app.domain.usecase.item.DeleteItemUseCase
import com.naoesqueci.app.domain.usecase.item.GetItemNameSuggestionsUseCase
import com.naoesqueci.app.domain.usecase.item.GetItemsUseCase
import com.naoesqueci.app.domain.usecase.item.GetItemsWithStateUseCase
import com.naoesqueci.app.domain.usecase.item.ToggleItemCheckUseCase
import com.naoesqueci.app.domain.usecase.item.UpdateItemUseCase
import com.naoesqueci.app.domain.usecase.notification.CancelTripNotificationsUseCase
import com.naoesqueci.app.domain.usecase.notification.ScheduleTripNotificationsUseCase
import com.naoesqueci.app.domain.usecase.settings.GetAdvanceTimeUseCase
import com.naoesqueci.app.domain.usecase.settings.GetRepeatMinutesUseCase
import com.naoesqueci.app.domain.usecase.settings.GetNotificationsEnabledUseCase
import com.naoesqueci.app.domain.usecase.settings.GetThemeUseCase
import com.naoesqueci.app.domain.usecase.settings.UpdateAdvanceTimeUseCase
import com.naoesqueci.app.domain.usecase.settings.UpdateRepeatMinutesUseCase
import com.naoesqueci.app.domain.usecase.settings.UpdateNotificationsEnabledUseCase
import com.naoesqueci.app.domain.usecase.settings.UpdateThemeUseCase
import com.naoesqueci.app.domain.usecase.trip.CreateTripUseCase
import com.naoesqueci.app.domain.usecase.trip.DeleteTripCascadeUseCase
import com.naoesqueci.app.domain.usecase.trip.GetTripUseCase
import com.naoesqueci.app.domain.usecase.trip.GetTripsUseCase
import com.naoesqueci.app.domain.usecase.trip.UpdateTripUseCase
import com.naoesqueci.app.presentation.ui.screen.item_edit.ItemEditScreen
import com.naoesqueci.app.presentation.ui.screen.item_edit.ItemEditViewModel
import com.naoesqueci.app.presentation.ui.screen.onboarding.OnboardingScreen
import com.naoesqueci.app.presentation.ui.screen.onboarding.OnboardingViewModel
import com.naoesqueci.app.presentation.ui.screen.settings.SettingsScreen
import com.naoesqueci.app.presentation.ui.screen.settings.SettingsViewModel
import com.naoesqueci.app.presentation.ui.screen.trip_create.TripCreateScreen
import com.naoesqueci.app.presentation.ui.screen.trip_create.TripCreateViewModel
import com.naoesqueci.app.presentation.ui.screen.trip_detail.TripDetailScreen
import com.naoesqueci.app.presentation.ui.screen.trip_detail.TripDetailViewModel
import com.naoesqueci.app.presentation.ui.screen.trip_list.TripListScreen
import com.naoesqueci.app.presentation.ui.screen.trip_list.TripListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    deepLinkTripId: StateFlow<Long?> = remember { MutableStateFlow<Long?>(null) },
    onDeepLinkConsumed: () -> Unit = {}
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val settingsDataStore = remember { SettingsDataStore(context) }

    val tripRepository = remember { TripRepositoryImpl(database.tripDao()) }
    val tripItemRepository = remember { TripItemRepositoryImpl(database.tripItemDao()) }
    val checkStateRepository = remember { CheckStateRepositoryImpl(database.checkStateDao()) }
    val notificationEventRepository = remember { NotificationEventRepositoryImpl(database.notificationEventDao()) }
    val settingsRepository = remember { SettingsRepositoryImpl(settingsDataStore) }

    val createTripUseCase = remember { CreateTripUseCase(tripRepository) }
    val updateTripUseCase = remember { UpdateTripUseCase(tripRepository) }
    val getTripUseCase = remember { GetTripUseCase(tripRepository) }
    val getTripsUseCase = remember { GetTripsUseCase(tripRepository) }

    val alarmScheduler = remember { AlarmScheduler(context, settingsRepository) }
    val deleteTripUseCase = remember {
        DeleteTripCascadeUseCase(
            tripRepository = tripRepository,
            tripItemRepository = tripItemRepository,
            checkStateRepository = checkStateRepository,
            notificationEventRepository = notificationEventRepository,
            alarmScheduler = alarmScheduler
        )
    }

    val addItemUseCase = remember { AddItemUseCase(tripItemRepository) }
    val updateItemUseCase = remember { UpdateItemUseCase(tripItemRepository) }
    val deleteItemUseCase = remember { DeleteItemUseCase(tripItemRepository) }
    val getItemsUseCase = remember { GetItemsUseCase(tripItemRepository) }
    val suggestNamesUseCase = remember { GetItemNameSuggestionsUseCase(tripItemRepository) }
    val getItemsWithStateUseCase = remember { GetItemsWithStateUseCase(tripItemRepository, checkStateRepository) }
    val toggleItemCheckUseCase = remember { ToggleItemCheckUseCase(checkStateRepository) }

    val scheduleNotificationsUseCase = remember { ScheduleTripNotificationsUseCase(alarmScheduler) }
    val cancelNotificationsUseCase = remember { CancelTripNotificationsUseCase(alarmScheduler) }

    val getThemeUseCase = remember { GetThemeUseCase(settingsRepository) }
    val updateThemeUseCase = remember { UpdateThemeUseCase(settingsRepository) }
    val getNotificationsEnabledUseCase = remember { GetNotificationsEnabledUseCase(settingsRepository) }
    val updateNotificationsEnabledUseCase = remember { UpdateNotificationsEnabledUseCase(settingsRepository) }
    val getAdvanceTimeUseCase = remember { GetAdvanceTimeUseCase(settingsRepository) }
    val updateAdvanceTimeUseCase = remember { UpdateAdvanceTimeUseCase(settingsRepository) }
    val getRepeatMinutesUseCase = remember { GetRepeatMinutesUseCase(settingsRepository) }
    val updateRepeatMinutesUseCase = remember { UpdateRepeatMinutesUseCase(settingsRepository) }

    val pendingDeepLinkTripId by deepLinkTripId.collectAsState()
    LaunchedEffect(pendingDeepLinkTripId) {
        pendingDeepLinkTripId?.let { tripId ->
            navController.navigate(AppRoute.TripDetail(tripId).route)
            onDeepLinkConsumed()
        }
    }

    NavHost(navController, startDestination = AppRoute.Onboarding.route) {
        composable(AppRoute.Onboarding.route) {
            val viewModel = remember { OnboardingViewModel(context.applicationContext) }
            OnboardingScreen(
                viewModel = viewModel,
                onFinish = {
                    navController.navigate(AppRoute.TripList.route) {
                        popUpTo(AppRoute.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppRoute.TripList.route) {
            val viewModel = remember {
                TripListViewModel(
                    getTripsUseCase = getTripsUseCase,
                    deleteTripUseCase = deleteTripUseCase,
                    getThemeUseCase = getThemeUseCase
                )
            }
            TripListScreen(
                viewModel = viewModel,
                onTripClick = { tripId -> navController.navigate(AppRoute.TripDetail(tripId).route) },
                onCreateTrip = { navController.navigate(AppRoute.TripCreate(null).route) },
                onSettingsClick = { navController.navigate(AppRoute.Settings.route) }
            )
        }

        composable(
            route = "trip_create/{tripId}",
            arguments = listOf(
                androidx.navigation.navArgument("tripId") {
                    type = androidx.navigation.NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: -1L
            val viewModel = remember {
                TripCreateViewModel(
                    createTripUseCase = createTripUseCase,
                    updateTripUseCase = updateTripUseCase,
                    getTripUseCase = getTripUseCase,
                    scheduleNotificationsUseCase = scheduleNotificationsUseCase,
                    cancelNotificationsUseCase = cancelNotificationsUseCase,
                    tripId = if (tripId == -1L) null else tripId
                )
            }
            TripCreateScreen(
                viewModel = viewModel,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = "trip_detail/{tripId}",
            arguments = listOf(
                androidx.navigation.navArgument("tripId") {
                    type = androidx.navigation.NavType.LongType
                }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val viewModel = remember {
                TripDetailViewModel(
                    getTripUseCase = getTripUseCase,
                    getItemsWithStateUseCase = getItemsWithStateUseCase,
                    toggleItemCheckUseCase = toggleItemCheckUseCase,
                    deleteItemUseCase = deleteItemUseCase,
                    deleteTripUseCase = deleteTripUseCase,
                    tripId = tripId
                )
            }
            TripDetailScreen(
                viewModel = viewModel,
                onEditTrip = { navController.navigate(AppRoute.TripCreate(tripId).route) },
                onAddItem = { navController.navigate(AppRoute.ItemEdit(tripId, null).route) },
                onEditItem = { itemId -> navController.navigate(AppRoute.ItemEdit(tripId, itemId).route) },
                onTripDeleted = { navController.popBackStack() }
            )
        }

        composable(
            route = "item_edit/{tripId}/{itemId}",
            arguments = listOf(
                androidx.navigation.navArgument("tripId") {
                    type = androidx.navigation.NavType.LongType
                },
                androidx.navigation.navArgument("itemId") {
                    type = androidx.navigation.NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: return@composable
            val itemId = backStackEntry.arguments?.getLong("itemId") ?: -1L
            val viewModel = remember {
                ItemEditViewModel(
                    addItemUseCase = addItemUseCase,
                    updateItemUseCase = updateItemUseCase,
                    deleteItemUseCase = deleteItemUseCase,
                    getItemUseCase = { id ->
                        runBlocking {
                            tripItemRepository.getItemById(id).first()
                        }
                    },
                    getItemsUseCase = getItemsUseCase,
                    suggestNamesUseCase = suggestNamesUseCase,
                    tripId = tripId,
                    itemId = if (itemId == -1L) null else itemId
                )
            }
            ItemEditScreen(
                viewModel = viewModel,
                onSave = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(AppRoute.Settings.route) {
            val viewModel = remember {
                SettingsViewModel(
                    getThemeUseCase = getThemeUseCase,
                    updateThemeUseCase = updateThemeUseCase,
                    getNotificationsEnabledUseCase = getNotificationsEnabledUseCase,
                    updateNotificationsEnabledUseCase = updateNotificationsEnabledUseCase,
                    getAdvanceTimeUseCase = getAdvanceTimeUseCase,
                    updateAdvanceTimeUseCase = updateAdvanceTimeUseCase,
                    getRepeatMinutesUseCase = getRepeatMinutesUseCase,
                    updateRepeatMinutesUseCase = updateRepeatMinutesUseCase
                )
            }
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}