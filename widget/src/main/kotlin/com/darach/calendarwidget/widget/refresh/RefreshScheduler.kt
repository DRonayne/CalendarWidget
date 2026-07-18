package com.darach.calendarwidget.widget.refresh

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.darach.calendarwidget.widget.receivers.RefreshAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Inexact boundary alarms — deliberately no SCHEDULE_EXACT_ALARM permission.
 * setAndAllowWhileIdle with RTC: the redraw lands within minutes of the
 * boundary (or on wake), which an agenda list tolerates by design.
 */
@Singleton
class RefreshScheduler
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        private val alarmManager: AlarmManager = requireNotNull(context.getSystemService(AlarmManager::class.java))

        fun schedule(at: Instant) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC, at.toEpochMilli(), pendingIntent())
        }

        fun cancel() {
            alarmManager.cancel(pendingIntent())
        }

        private fun pendingIntent(): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, RefreshAlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
    }
