package com.darach.calendarwidget.widget.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * System conditions that invalidate the rendered agenda: reboot (alarms are
 * lost), clock/zone/locale changes, and calendar provider changes while our
 * in-process ContentObserver is dead.
 */
@AndroidEntryPoint
class SystemEventsReceiver : BroadcastReceiver() {
    @Inject
    lateinit var refresher: WidgetRefresher

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val reason =
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED -> RefreshReason.BOOT

                Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_DATE_CHANGED,
                Intent.ACTION_LOCALE_CHANGED,
                -> RefreshReason.TIME_CHANGED

                Intent.ACTION_PROVIDER_CHANGED -> RefreshReason.CALENDAR_DATA_CHANGED

                else -> null
            }
        reason?.let(refresher::requestRefresh)
    }
}
