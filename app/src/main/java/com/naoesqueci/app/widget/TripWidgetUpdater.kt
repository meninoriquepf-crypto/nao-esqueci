package com.naoesqueci.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.naoesqueci.app.MainActivity
import com.naoesqueci.app.R
import com.naoesqueci.app.data.local.database.AppDatabase
import com.naoesqueci.app.domain.model.ItemCategory
import com.naoesqueci.app.domain.model.TripType
import com.naoesqueci.app.domain.util.DateTimeUtils
import kotlinx.coroutines.flow.first

object TripWidgetUpdater {

    suspend fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, TripWidgetProvider::class.java))
        ids.forEach { updateOne(context, manager, it) }
    }

    suspend fun updateOne(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
        manager.updateAppWidget(appWidgetId, buildViews(context))
    }

    private suspend fun buildViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_trip)
        val db = AppDatabase.getDatabase(context)
        val now = System.currentTimeMillis()

        val tripEntity = db.tripDao().getCurrent(now).first().firstOrNull()
            ?: db.tripDao().getUpcoming(now).first().firstOrNull()

        if (tripEntity == null) {
            views.setTextViewText(R.id.widget_trip_name, context.getString(R.string.widget_empty_title))
            views.setTextViewText(R.id.widget_trip_dates, context.getString(R.string.widget_empty_subtitle))
            views.setViewVisibility(R.id.widget_trip_progress, View.GONE)
            views.setViewVisibility(R.id.widget_trip_count, View.GONE)
            views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context, null))
            return views
        }

        val tripType = if (now < tripEntity.departureDateTime) TripType.DEPARTURE else TripType.RETURN
        val items = db.tripItemDao().getByTripId(tripEntity.id).first()
        val required = items.filter {
            val category = try { ItemCategory.valueOf(it.category) } catch (e: Exception) { ItemCategory.NORMAL }
            val requiredForType = when (tripType) {
                TripType.DEPARTURE -> it.requiredForDeparture
                TripType.RETURN -> it.requiredForReturn
            }
            category == ItemCategory.NORMAL && requiredForType
        }
        val checkedIds = db.checkStateDao().getByTripAndType(tripEntity.id, tripType.name).first()
            .filter { it.isChecked }
            .map { it.itemId }
            .toSet()
        val checked = required.count { checkedIds.contains(it.id) }

        // "Ida dd/MM às HH:mm • Volta dd/MM às HH:mm"
        val dep = "${DateTimeUtils.formatDate(tripEntity.departureDateTime)} às ${DateTimeUtils.formatTime(tripEntity.departureDateTime)}"
        val ret = "${DateTimeUtils.formatDate(tripEntity.returnDateTime)} às ${DateTimeUtils.formatTime(tripEntity.returnDateTime)}"
        val dates = "$dep • $ret"

        views.setTextViewText(R.id.widget_trip_name, tripEntity.name)
        views.setTextViewText(R.id.widget_trip_dates, dates)

        if (required.isEmpty()) {
            views.setViewVisibility(R.id.widget_trip_progress, View.GONE)
            views.setViewVisibility(R.id.widget_trip_count, View.VISIBLE)
            views.setTextViewText(R.id.widget_trip_count, context.getString(R.string.widget_no_items))
        } else {
            views.setViewVisibility(R.id.widget_trip_progress, View.VISIBLE)
            views.setViewVisibility(R.id.widget_trip_count, View.VISIBLE)
            views.setProgressBar(R.id.widget_trip_progress, 100, (checked * 100 / required.size), false)
            val countText = if (checked >= required.size) {
                context.getString(R.string.trip_detail_all_checked)
            } else {
                context.getString(R.string.trip_detail_progress, checked, required.size)
            }
            views.setTextViewText(R.id.widget_trip_count, countText)
        }

        views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context, tripEntity.id))
        return views
    }

    private fun openAppIntent(context: Context, tripId: Long?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            tripId?.let { putExtra("DEEP_LINK_TRIP_ID", it) }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            tripId?.toInt() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
