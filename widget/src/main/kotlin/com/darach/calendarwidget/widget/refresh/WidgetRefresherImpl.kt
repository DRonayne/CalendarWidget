package com.darach.calendarwidget.widget.refresh

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Every refresh trigger funnels into one unique expedited worker — the
 * platform-native serializer for the update pipeline. Config saves REPLACE
 * (the user expects their change to win); everything else coalesces via KEEP.
 *
 * KEEP drops requests that arrive while a run is already executing, so each
 * enqueue also bumps [generation]; the worker compares it at the end of a run
 * and asks for one [enqueueFollowUp] pass if anything arrived mid-run.
 */
@Singleton
class WidgetRefresherImpl
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : WidgetRefresher {
        private val generation = AtomicLong()

        /** Monotonic count of refresh requests; see class doc. */
        fun generation(): Long = generation.get()

        override fun requestRefresh(reason: RefreshReason) {
            val policy =
                when (reason) {
                    RefreshReason.CONFIG_CHANGED -> ExistingWorkPolicy.REPLACE
                    else -> ExistingWorkPolicy.KEEP
                }
            enqueue(policy)
        }

        override suspend fun refreshAndAwait(reason: RefreshReason): Boolean {
            // Always REPLACE: awaiting only makes sense if this request runs.
            val request = enqueue(ExistingWorkPolicy.REPLACE)
            // Await this specific request — the unique-work query can briefly
            // surface the REPLACEd predecessor, whose CANCELLED state already
            // counts as finished.
            val finished =
                WorkManager
                    .getInstance(context)
                    .getWorkInfoByIdFlow(request.id)
                    .filterNotNull()
                    .first { it.state.isFinished }
            return finished.state == WorkInfo.State.SUCCEEDED
        }

        /**
         * Chains exactly one more pipeline run after the current one, for
         * requests KEEP dropped mid-run. Not expedited: expedited work cannot
         * be part of an APPEND chain, and this runs right afterwards anyway.
         */
        fun enqueueFollowUp() {
            val request =
                OneTimeWorkRequestBuilder<AgendaRefreshWorker>()
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
                    .build()
            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.APPEND_OR_REPLACE, request)
        }

        private fun enqueue(policy: ExistingWorkPolicy): OneTimeWorkRequest {
            val request =
                OneTimeWorkRequestBuilder<AgendaRefreshWorker>()
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
                    .build()
            // Bump before enqueueing: a worker that finishes in between sees
            // the bump and schedules a follow-up covering this request.
            generation.incrementAndGet()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request)
            return request
        }

        companion object {
            const val WORK_NAME = "agenda-refresh"
            private const val BACKOFF_SECONDS = 30L
        }
    }
