package com.darach.calendarwidget.widget.ui

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.net.toUri
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import com.darach.calendarwidget.core.model.CalendarEvent

/** Tap targets — all deep-link into the system calendar app (the clone behavior). */
internal object WidgetActions {
    // Intents are built with apply {} (not builder chaining) so the Glance
    // JVM unit tests, which stub android.jar with default return values, work.
    fun openEvent(event: CalendarEvent): Action {
        // Uri vals stay nullable: android.jar test stubs return null from static
        // helpers, and Intent accepts a null data uri.
        val uri: Uri? = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.eventId)
        val intent =
            Intent(Intent.ACTION_VIEW, uri).apply {
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.startsAt.toEpochMilli())
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.endsAt.toEpochMilli())
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        return actionStartActivity(intent)
    }

    fun openDay(epochMillis: Long): Action {
        val uri: Uri? = "content://com.android.calendar/time/$epochMillis".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        return actionStartActivity(intent)
    }

    fun newEvent(): Action {
        val intent =
            Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        return actionStartActivity(intent)
    }

    fun openApp(packageName: String): Action {
        val intent =
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        return actionStartActivity(intent)
    }
}
