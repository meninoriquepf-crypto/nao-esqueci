package com.naoesqueci.app.domain.model

data class CheckState(
    val tripId: Long,
    val itemId: Long,
    val tripType: TripType,
    val isChecked: Boolean = false,
    val checkedAt: Long = System.currentTimeMillis()
) {
    fun copyChecked(checked: Boolean) = copy(isChecked = checked, checkedAt = System.currentTimeMillis())
}