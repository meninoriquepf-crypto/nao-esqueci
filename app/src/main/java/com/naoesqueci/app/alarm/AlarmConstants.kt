package com.naoesqueci.app.alarm

object AlarmConstants {
    const val EXTRA_TRIP_ID = "TRIP_ID"
    const val EXTRA_TRIP_TYPE = "TRIP_TYPE"
    const val EXTRA_EVENT_TYPE = "EVENT_TYPE"
    const val EXTRA_ADVANCE_MINUTES = "ADVANCE_MINUTES"

    fun generateAction(tripId: Long, tripType: String, eventType: String): String {
        return "com.naoesqueci.app.ALARM_${tripId}_${tripType}_${eventType}"
    }

    fun generateRequestCode(tripId: Long, tripTypeOrdinal: Int, eventTypeOrdinal: Int): Int {
        return (tripId.toInt() shl 3) or (tripTypeOrdinal shl 2) or eventTypeOrdinal
    }
}