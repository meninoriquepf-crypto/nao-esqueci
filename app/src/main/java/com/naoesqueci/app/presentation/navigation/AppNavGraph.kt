package com.naoesqueci.app.presentation.navigation

sealed class AppRoute(val route: String) {
    object Onboarding : AppRoute("onboarding")
    object TripList : AppRoute("trip_list")
    data class TripCreate(val tripId: Long? = null) : AppRoute("trip_create/${tripId?.toString() ?: "-1"}")
    data class TripDetail(val tripId: Long) : AppRoute("trip_detail/$tripId")
    data class ItemEdit(val tripId: Long, val itemId: Long? = null) : AppRoute("item_edit/$tripId/${itemId?.toString() ?: "-1"}")
    object Settings : AppRoute("settings")
}