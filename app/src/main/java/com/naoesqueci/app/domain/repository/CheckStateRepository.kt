package com.naoesqueci.app.domain.repository

import com.naoesqueci.app.domain.model.CheckState
import kotlinx.coroutines.flow.Flow

interface CheckStateRepository {
    suspend fun upsertCheckState(checkState: CheckState)
    suspend fun upsertCheckStates(checkStates: List<CheckState>)
    fun getCheckStatesByTripAndType(tripId: Long, tripType: String): Flow<List<CheckState>>
    fun getAllCheckStatesByTrip(tripId: Long): Flow<List<CheckState>>
    suspend fun deleteCheckStatesByTripId(tripId: Long)
    suspend fun deleteCheckStateByTripAndItem(tripId: Long, itemId: Long)
    fun getCheckState(tripId: Long, itemId: Long, tripType: String): Flow<CheckState?>
}