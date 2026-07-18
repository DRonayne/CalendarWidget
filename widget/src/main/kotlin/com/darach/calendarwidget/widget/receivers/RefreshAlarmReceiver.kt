package com.darach.calendarwidget.widget.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Fired by the inexact boundary alarm; hands straight off to the unique worker. */
@AndroidEntryPoint
class RefreshAlarmReceiver : BroadcastReceiver() {
    @Inject
    lateinit var refresher: WidgetRefresher

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        refresher.requestRefresh(RefreshReason.ALARM)
    }
}
