package com.naoesqueci.app.domain.util

import com.naoesqueci.app.data.local.database.dao.CheckStateDao
import com.naoesqueci.app.data.local.database.dao.TripItemDao
import com.naoesqueci.app.domain.model.ItemCategory
import com.naoesqueci.app.domain.model.TripType
import kotlinx.coroutines.flow.first

object NotificationUtils {

    suspend fun getPendingItemNames(
        tripItemDao: TripItemDao,
        checkStateDao: CheckStateDao,
        tripId: Long,
        tripType: TripType
    ): List<String> {
        val items = tripItemDao.getByTripId(tripId).first()
        val requiredItems = items.filter { item ->
            item.category != ItemCategory.GIFT.name && when (tripType) {
                TripType.DEPARTURE -> item.requiredForDeparture
                TripType.RETURN -> item.requiredForReturn
            }
        }

        if (requiredItems.isEmpty()) return emptyList()

        val checkedStates = checkStateDao.getByTripAndType(tripId, tripType.name).first()
        val checkedItemIds = checkedStates.filter { it.isChecked }.map { it.itemId }.toSet()

        return requiredItems.filter { it.id !in checkedItemIds }.map { it.name }
    }

    suspend fun hasPendingRequiredItems(
        tripItemDao: TripItemDao,
        checkStateDao: CheckStateDao,
        tripId: Long,
        tripType: TripType
    ): Boolean {
        return getPendingItemNames(tripItemDao, checkStateDao, tripId, tripType).isNotEmpty()
    }
}