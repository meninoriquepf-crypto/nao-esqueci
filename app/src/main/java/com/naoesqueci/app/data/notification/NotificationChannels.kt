package com.naoesqueci.app.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.naoesqueci.app.R

object NotificationChannels {
    const val CHANNEL_DEPARTURE = "channel_departure"
    const val CHANNEL_RETURN = "channel_return"
    const val CHANNEL_POSITIVE = "channel_positive"

    fun createChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val departureChannel = NotificationChannel(
            CHANNEL_DEPARTURE,
            context.getString(R.string.notification_channel_departure),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_departure_desc)
            enableVibration(true)
        }

        val returnChannel = NotificationChannel(
            CHANNEL_RETURN,
            context.getString(R.string.notification_channel_return),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_return_desc)
            enableVibration(true)
        }

        val positiveChannel = NotificationChannel(
            CHANNEL_POSITIVE,
            context.getString(R.string.notification_channel_positive),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_positive_desc)
        }

        manager.createNotificationChannels(listOf(departureChannel, returnChannel, positiveChannel))
    }
}