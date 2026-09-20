package com.naoesqueci.app.data.local.backup

import android.content.Context
import android.net.Uri
import com.naoesqueci.app.data.local.database.AppDatabase
import com.naoesqueci.app.data.local.database.entity.TripEntity
import com.naoesqueci.app.data.local.database.entity.TripItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject

class BackupManager(private val context: Context) {

    data class ImportResult(val trips: Int, val items: Int)

    suspend fun exportTo(uri: Uri): Int = withContext(Dispatchers.IO) {
        val db = AppDatabase.getDatabase(context)
        val trips = db.tripDao().getAll().first()

        val tripsJson = JSONArray()
        trips.forEach { trip ->
            val items = db.tripItemDao().getByTripId(trip.id).first()
            val checks = db.checkStateDao().getAllByTrip(trip.id).first()
            val itemsJson = JSONArray()
            items.forEach { item ->
                itemsJson.put(
                    JSONObject()
                        .put("name", item.name)
                        .put("category", item.category)
                        .put("requiredForDeparture", item.requiredForDeparture)
                        .put("requiredForReturn", item.requiredForReturn)
                        .put("displayOrder", item.displayOrder)
                        .put(
                            "checkedDeparture",
                            checks.any { it.itemId == item.id && it.tripType == "DEPARTURE" && it.isChecked }
                        )
                        .put(
                            "checkedReturn",
                            checks.any { it.itemId == item.id && it.tripType == "RETURN" && it.isChecked }
                        )
                )
            }
            tripsJson.put(
                JSONObject()
                    .put("name", trip.name)
                    .put("departureDateTime", trip.departureDateTime)
                    .put("returnDateTime", trip.returnDateTime)
                    .put("createdAt", trip.createdAt)
                    .put("items", itemsJson)
            )
        }

        val root = JSONObject()
            .put("version", 1)
            .put("exportedAt", System.currentTimeMillis())
            .put("trips", tripsJson)

        context.contentResolver.openOutputStream(uri)?.use { out ->
            out.write(root.toString().toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("backup_output")

        trips.size
    }

    suspend fun importFrom(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        } ?: throw IllegalArgumentException("backup_input")

        val root = try {
            JSONObject(text)
        } catch (e: Exception) {
            throw IllegalArgumentException("backup_json", e)
        }
        if (root.optInt("version", -1) != 1) throw IllegalArgumentException("backup_version")
        val tripsJson = root.optJSONArray("trips") ?: throw IllegalArgumentException("backup_trips")

        val db = AppDatabase.getDatabase(context)
        var tripCount = 0
        var itemCount = 0
        val now = System.currentTimeMillis()

        db.withTransaction {
            for (i in 0 until tripsJson.length()) {
                val tripJson = tripsJson.getJSONObject(i)
                val name = tripJson.optString("name", "").trim()
                if (name.isEmpty()) continue
                val newTripId = db.tripDao().insert(
                    TripEntity(
                        name = name,
                        departureDateTime = tripJson.optLong("departureDateTime", now),
                        returnDateTime = tripJson.optLong("returnDateTime", now + 86400000),
                        createdAt = tripJson.optLong("createdAt", now),
                        updatedAt = now
                    )
                )
                tripCount++

                val itemsJson = tripJson.optJSONArray("items") ?: JSONArray()
                for (j in 0 until itemsJson.length()) {
                    val itemJson = itemsJson.getJSONObject(j)
                    val itemName = itemJson.optString("name", "").trim()
                    if (itemName.isEmpty()) continue
                    val newItemId = db.tripItemDao().insert(
                        TripItemEntity(
                            tripId = newTripId,
                            name = itemName,
                            category = itemJson.optString("category", "NORMAL"),
                            requiredForDeparture = itemJson.optBoolean("requiredForDeparture", true),
                            requiredForReturn = itemJson.optBoolean("requiredForReturn", true),
                            displayOrder = itemJson.optInt("displayOrder", j),
                            createdAt = now,
                            updatedAt = now
                        )
                    )
                    itemCount++
                    if (itemJson.optBoolean("checkedDeparture", false)) {
                        db.checkStateDao().upsert(
                            com.naoesqueci.app.data.local.database.entity.CheckStateEntity(
                                tripId = newTripId,
                                itemId = newItemId,
                                tripType = "DEPARTURE",
                                isChecked = true,
                                checkedAt = now
                            )
                        )
                    }
                    if (itemJson.optBoolean("checkedReturn", false)) {
                        db.checkStateDao().upsert(
                            com.naoesqueci.app.data.local.database.entity.CheckStateEntity(
                                tripId = newTripId,
                                itemId = newItemId,
                                tripType = "RETURN",
                                isChecked = true,
                                checkedAt = now
                            )
                        )
                    }
                }
            }
        }

        ImportResult(tripCount, itemCount)
    }
}
