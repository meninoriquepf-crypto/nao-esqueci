package com.naoesqueci.app.domain.model

data class Trip(
    val id: Long? = null,
    val name: String,
    val departureDateTime: Long,
    val returnDateTime: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    fun isDepartureInFuture(): Boolean = departureDateTime > System.currentTimeMillis()
    fun isReturnInFuture(): Boolean = returnDateTime > System.currentTimeMillis()
    fun copyWithUpdatedTimestamps() = copy(updatedAt = System.currentTimeMillis())
}