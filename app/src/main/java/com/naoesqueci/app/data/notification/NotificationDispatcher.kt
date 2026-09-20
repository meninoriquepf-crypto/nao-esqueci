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

    fun sendFinalAlert(trip: Trip, type: TripType, pendingItems: List<String>) {
        val itemList = pendingItems.take(3).joinToString(", ") +
            if (pendingItems.size > 3) " e mais ${pendingItems.size - 3}" else ""

        val titleRes = when (type) {
            TripType.DEPARTURE -> R.string.notification_final_departure_title
            TripType.RETURN -> R.string.notification_final_return_title
        }
        val bodyRes = when (type) {
            TripType.DEPARTURE -> R.string.notification_pending_departure_body
            TripType.RETURN -> R.string.notification_pending_return_body
        }
        val channelId = if (type == TripType.DEPARTURE) NotificationChannels.CHANNEL_DEPARTURE else NotificationChannels.CHANNEL_RETURN
        val tripId = trip.id ?: return

        val contentIntent = PendingIntent.getActivity(
            context,
            deepLinkRequestCode(tripId, type),
            deepLinkIntent(tripId, type),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val fullScreenIntent = PendingIntent.getActivity(
            context,
            deepLinkRequestCode(tripId, type) + 1000,
            deepLinkIntent(tripId, type),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val snoozeIntent = android.content.Intent(context, com.naoesqueci.app.alarm.AlarmReceiver::class.java).apply {
            action = com.naoesqueci.app.alarm.AlarmConstants.generateAction(tripId, type.name, "SNOOZE")
            putExtra(com.naoesqueci.app.alarm.AlarmConstants.EXTRA_TRIP_ID, tripId)
            putExtra(com.naoesqueci.app.alarm.AlarmConstants.EXTRA_TRIP_TYPE, type.name)
            putExtra(com.naoesqueci.app.alarm.AlarmConstants.EXTRA_EVENT_TYPE, "SNOOZE")
            putExtra(com.naoesqueci.app.alarm.AlarmConstants.EXTRA_DEFERRED, true)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            com.naoesqueci.app.alarm.AlarmConstants.generateRequestCode(tripId, type.ordinal, 3),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(titleRes))
            .setContentText(context.getString(bodyRes, itemList))
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(context.getString(bodyRes, itemList)))
            .setPriority(android.app.Notification.PRIORITY_MAX)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(contentIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .addAction(0, context.getString(R.string.notification_snooze), snoozePendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        manager.notify(com.naoesqueci.app.alarm.AlarmConstants.finalNotificationId(tripId, type.ordinal), notification)
    }

    fun dismissFinalAlert(tripId: Long, type: TripType) {
        manager.cancel(com.naoesqueci.app.alarm.AlarmConstants.finalNotificationId(tripId, type.ordinal))
    }

    private fun deepLinkIntent(tripId: Long, type: TripType): Intent {
        return Intent(context, MainActivity::class.java).apply {
            putExtra("DEEP_LINK_TRIP_ID", tripId)
            putExtra("DEEP_LINK_TRIP_TYPE", type.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    }

    private fun deepLinkRequestCode(tripId: Long, type: TripType): Int {
        return (tripId.toInt() shl 3) or (type.ordinal shl 2)
    }

    fun sendPositiveConfirmation(trip: Trip, type: TripType) {        val titleRes = when (type) {
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