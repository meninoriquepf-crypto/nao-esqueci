package com.naoesqueci.app.data.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.naoesqueci.app.MainActivity
import com.naoesqueci.app.R
import com.naoesqueci.app.domain.model.Trip
import com.naoesqueci.app.domain.model.TripType
import com.naoesqueci.app.domain.repository.NotificationEventRepository
import com.naoesqueci.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class NotificationDispatcher(
    private val context: Context,
    private val eventRepository: NotificationEventRepository,
    private val settingsRepository: SettingsRepository
) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        NotificationChannels.createChannels(context)
    }

    fun sendAdvanceWarning(trip: Trip, type: TripType) {
        val titleRes = when (type) {
            TripType.DEPARTURE -> R.string.notification_advance_departure_title
            TripType.RETURN -> R.string.notification_advance_return_title
        }
        val bodyRes = when (type) {
            TripType.DEPARTURE -> R.string.notification_advance_departure_body
            TripType.RETURN -> R.string.notification_advance_return_body
        }
        val advance = getAdvanceMinutes()

        sendNotification(
            tripId = trip.id ?: return,
            type = type,
            title = context.getString(titleRes),
            body = context.getString(bodyRes, advance),
            channelId = if (type == TripType.DEPARTURE) NotificationChannels.CHANNEL_DEPARTURE else NotificationChannels.CHANNEL_RETURN,
            priority = android.app.Notification.PRIORITY_HIGH
        )
    }

    fun sendPendingItemsAlert(trip: Trip, type: TripType, pendingItems: List<String>) {
        val itemList = pendingItems.take(3).joinToString(", ") +
            if (pendingItems.size > 3) " e mais ${pendingItems.size - 3}" else ""

        val titleRes = when (type) {
            TripType.DEPARTURE -> R.string.notification_pending_departure_title
            TripType.RETURN -> R.string.notification_pending_return_title
        }
        val bodyRes = when (type) {
            TripType.DEPARTURE -> R.string.notification_pending_departure_body
            TripType.RETURN -> R.string.notification_pending_return_body
        }

        sendNotification(
            tripId = trip.id ?: return,
            type = type,
            title = context.getString(titleRes),
            body = context.getString(bodyRes, itemList),
            channelId = if (type == TripType.DEPARTURE) NotificationChannels.CHANNEL_DEPARTURE else NotificationChannels.CHANNEL_RETURN,
            priority = android.app.Notification.PRIORITY_HIGH
        )
    }

    fun sendPositiveConfirmation(trip: Trip, type: TripType) {
        val titleRes = when (type) {
            TripType.DEPARTURE -> R.string.notification_positive_departure_title
            TripType.RETURN -> R.string.notification_positive_return_title
        }
        val bodyRes = when (type) {
            TripType.DEPARTURE -> R.string.notification_positive_departure_body
            TripType.RETURN -> R.string.notification_positive_return_body
        }

        sendNotification(
            tripId = trip.id ?: return,
            type = type,
            title = context.getString(titleRes),
            body = context.getString(bodyRes),
            channelId = NotificationChannels.CHANNEL_POSITIVE,
            priority = android.app.Notification.PRIORITY_DEFAULT
        )
    }

    private fun sendNotification(
        tripId: Long,
        type: TripType,
        title: String,
        body: String,
        channelId: String,
        priority: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("DEEP_LINK_TRIP_ID", tripId)
            putExtra("DEEP_LINK_TRIP_TYPE", type.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val notificationId = (tripId.toInt() shl 3) or (type.ordinal shl 2)
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
    }

    private fun getAdvanceMinutes(): Int {
        return try {
            kotlinx.coroutines.runBlocking { settingsRepository.advanceMinutes.first() }
        } catch (e: Exception) {
            30
        }
    }
}