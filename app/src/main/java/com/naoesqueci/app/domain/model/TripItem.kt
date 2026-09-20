package com.naoesqueci.app.domain.model

data class TripItem(
    val id: Long? = null,
    val tripId: Long,
    val name: String,
    val category: ItemCategory = ItemCategory.NORMAL,
    val requiredForDeparture: Boolean = true,
    val requiredForReturn: Boolean = true,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun requiredFor(type: TripType): Boolean = when (type) {
        TripType.DEPARTURE -> requiredForDeparture
        TripType.RETURN -> requiredForReturn
    }

    fun isRequiredForAny(): Boolean = requiredForDeparture || requiredForReturn
    fun copyWithUpdatedTimestamp() = copy(updatedAt = System.currentTimeMillis())
}