package com.naoesqueci.app.domain.usecase.item

import com.naoesqueci.app.domain.model.CheckState
import com.naoesqueci.app.domain.model.ItemWithState
import com.naoesqueci.app.domain.model.TripItem
import com.naoesqueci.app.domain.model.TripType
import com.naoesqueci.app.domain.repository.CheckStateRepository
import com.naoesqueci.app.domain.repository.TripItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class AddItemUseCase(private val repository: TripItemRepository) {
    suspend operator fun invoke(item: TripItem): Long = repository.addItem(item)
}

class UpdateItemUseCase(private val repository: TripItemRepository) {
    suspend operator fun invoke(item: TripItem) = repository.updateItem(item)
}

class DeleteItemUseCase(private val repository: TripItemRepository) {
    suspend operator fun invoke(itemId: Long) = repository.deleteItem(itemId)
}

class GetItemsUseCase(private val repository: TripItemRepository) {
    operator fun invoke(tripId: Long): Flow<List<TripItem>> = repository.getItemsByTripId(tripId)
}

class GetItemNameSuggestionsUseCase(private val repository: TripItemRepository) {
    operator fun invoke(prefix: String): Flow<List<String>> = repository.suggestItemNames(prefix)
}

class GetItemsWithStateUseCase(
    private val itemRepository: TripItemRepository,
    private val checkStateRepository: CheckStateRepository
) {
    operator fun invoke(tripId: Long): Flow<List<ItemWithState>> {
        return itemRepository.getItemsByTripId(tripId).combine(
            checkStateRepository.getAllCheckStatesByTrip(tripId)
        ) { items, checkStates ->
            items.map { item ->
                ItemWithState(
                    item = item,
                    checkState = checkStates.find { it.itemId == item.id }
                )
            }
        }
    }

    fun byType(tripId: Long, tripType: TripType): Flow<List<ItemWithState>> {
        return itemRepository.getItemsByTripId(tripId).combine(
            checkStateRepository.getCheckStatesByTripAndType(tripId, tripType.name)
        ) { items, checkStates ->
            items.map { item ->
                ItemWithState(
                    item = item,
                    checkState = checkStates.find { it.itemId == item.id }
                )
            }
        }
    }
}

class ToggleItemCheckUseCase(private val checkStateRepository: CheckStateRepository) {
    suspend operator fun invoke(tripId: Long, itemId: Long, tripType: TripType, isChecked: Boolean) {
        checkStateRepository.upsertCheckState(
            CheckState(
                tripId = tripId,
                itemId = itemId,
                tripType = tripType,
                isChecked = isChecked
            )
        )
    }
}