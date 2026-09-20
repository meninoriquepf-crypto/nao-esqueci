package com.naoesqueci.app.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_states")
data class CheckStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val itemId: Long,
    val tripType: String,
    val isChecked: Boolean = false,
    val checkedAt: Long = System.currentTimeMillis()
)