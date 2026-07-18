package com.darach.calendarwidget.widget.refresh

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Every refresh trigger funnels into one unique expedited worker — the
 * platform-native serializer for the update pipeline. Config saves REPLACE
 * (the user expects their change to win); everything else coalesces via KEEP.
 */
@Singleton
class WidgetRefresherImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : WidgetRefresher {
        override fun requestRefresh(reason: RefreshReason) {
            enqueue(reason)
        }

        override suspend fun refreshAndAwait(reason: RefreshReason): Boolean {
            enqueue(reason)
            val finished =
                WorkManager
                    .getInstance(context)
                    .getWorkInfosForUniqueWorkFlow(WORK_NAME)
                    .mapNotNull { infos -> infos.firstOrNull() }
                    .first { it.state.isFinished }
            return finished.state == WorkInfo.State.SUCCEEDED
        }

        private fun enqueue(reason: RefreshReason) {
            val policy =
                when (reason) {
                    RefreshReason.CONFIG_CHANGED -> ExistingWorkPolicy.REPLACE
                    else -> ExistingWorkPolicy.KEEP
                }
            val request =
                OneTimeWorkRequestBuilder<AgendaRefreshWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
        }

        companion object {
            const val WORK_NAME = "agenda-refresh"
            private const val BACKOFF_SECONDS = 30L
        }
    }
