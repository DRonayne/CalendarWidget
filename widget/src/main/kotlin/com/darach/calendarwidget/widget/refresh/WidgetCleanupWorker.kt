package com.darach.calendarwidget.widget.refresh

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.snapshot.SnapshotRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/** Drops per-instance config + snapshot when widget instances are deleted. */
@HiltWorker
class WidgetCleanupWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val configRepository: WidgetConfigRepository,
        private val snapshotRepository: SnapshotRepository,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val ids = inputData.getIntArray(KEY_WIDGET_IDS)?.toList() ?: return Result.success()
            configRepository.remove(ids)
            snapshotRepository.remove(ids)
            return Result.success()
        }

        companion object {
            private const val KEY_WIDGET_IDS = "widget_ids"

            fun enqueue(
                context: Context,
                appWidgetIds: IntArray,
            ) {
                val request =
                    OneTimeWorkRequestBuilder<WidgetCleanupWorker>()
                        .setInputData(Data.Builder().putIntArray(KEY_WIDGET_IDS, appWidgetIds).build())
                        .build()
                WorkManager.getInstance(context).enqueue(request)
            }
        }
    }
