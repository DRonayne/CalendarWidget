package com.darach.calendarwidget.widget.observer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Instant refresh while our process is alive; the PROVIDER_CHANGED manifest
 * receiver covers edits made while we are dead. Process-lifetime registration.
 */
@Singleton
class CalendarChangeObserver
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
        private val refresher: WidgetRefresher,
    ) {
        private val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    refresher.requestRefresh(RefreshReason.CALENDAR_DATA_CHANGED)
                }
            }

        @Volatile
        private var registered = false

        fun register() {
            if (registered) return
            val granted =
                context.checkSelfPermission(Manifest.permission.READ_CALENDAR) ==
                    PackageManager.PERMISSION_GRANTED
            if (granted) {
                context.contentResolver.registerContentObserver(CalendarContract.CONTENT_URI, true, observer)
                registered = true
            }
        }
    }
