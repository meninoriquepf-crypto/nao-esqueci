package com.naoesqueci.app.alarm

object AlarmConstants {
    const val EXTRA_TRIP_ID = "TRIP_ID"
    const val EXTRA_TRIP_TYPE = "TRIP_TYPE"
    const val EXTRA_EVENT_TYPE = "EVENT_TYPE"
    const val EXTRA_ADVANCE_MINUTES = "ADVANCE_MINUTES"
    const val EXTRA_DEFERRED = "DEFERRED"

    const val SNOOZE_MINUTES = 10

    fun generateAction(tripId: Long, tripType: String, eventType: String): String {
        return "com.naoesqueci.app.ALARM_${tripId}_${tripType}_${eventType}"
    }

    fun generateRequestCode(tripId: Long, tripTypeOrdinal: Int, eventTypeOrdinal: Int): Int {
        return (tripId.toInt() shl 3) or (tripTypeOrdinal shl 2) or eventTypeOrdinal
    }

    fun finalNotificationId(tripId: Long, tripTypeOrdinal: Int): Int {
        return ((tripId.toInt() shl 4) or (tripTypeOrdinal shl 3) or 0x7)
    }
}