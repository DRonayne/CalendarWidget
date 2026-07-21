package com.darach.calendarwidget.widget.refresh

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.repository.CalendarRepository
import com.darach.calendarwidget.core.data.snapshot.SnapshotRepository
import com.darach.calendarwidget.core.domain.AgendaWindow
import com.darach.calendarwidget.core.domain.BuildAgendaUseCase
import com.darach.calendarwidget.core.domain.ComputeNextRefreshUseCase
import com.darach.calendarwidget.core.model.AgendaSnapshot
import com.darach.calendarwidget.core.model.DomainError
import com.darach.calendarwidget.core.model.domainError
import com.darach.calendarwidget.widget.AgendaGlanceWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * The single serialized update pipeline:
 * query -> bucket -> snapshot -> update Glance -> schedule next boundary alarm.
 *
 * Each widget is refreshed independently: one widget's failure must never
 * block the redraw or the next alarm for the others.
 */
@HiltWorker
class AgendaRefreshWorker
    @AssistedInject
    constructor(
        @Assisted context: Context,
        @Assisted params: WorkerParameters,
        private val calendarRepository: CalendarRepository,
        private val configRepository: WidgetConfigRepository,
        private val snapshotRepository: SnapshotRepository,
        private val buildAgenda: BuildAgendaUseCase,
        private val computeNextRefresh: ComputeNextRefreshUseCase,
        private val scheduler: RefreshScheduler,
        private val refresher: WidgetRefresherImpl,
        private val instrumentation: RefreshInstrumentation,
        private val clock: Clock,
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            val generationAtStart = refresher.generation()
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(AgendaGlanceWidget::class.java)
            if (glanceIds.isEmpty()) {
                scheduler.cancel()
                return Result.success()
            }

            instrumentation.started(glanceIds.size)

            val store = configRepository.current()
            val zone = clock.zone
            val now = clock.instant()
            val today = now.atZone(zone).toLocalDate()

            var earliestBoundary: Instant? = null
            var transientFailure = false

            for (glanceId in glanceIds) {
                val appWidgetId = manager.getAppWidgetId(glanceId)
                val unexpected =
                    guarded {
                        val config = store.configFor(appWidgetId)
                        val window = AgendaWindow.from(config, today)
                        calendarRepository
                            .events(
                                window,
                                zone,
                                config.hiddenCalendarIds,
                                config.hideDeclined,
                                config.showAttendeePhotos,
                            ).onSuccess { events ->
                                val days = buildAgenda(events, window, zone, config.emptyDayBehavior)
                                snapshotRepository.put(appWidgetId, AgendaSnapshot(generatedAt = now, days = days))
                                val next = computeNextRefresh(events, now, zone)
                                earliestBoundary = earliestBoundary?.let { minOf(it, next) } ?: next
                            }.onFailure { failure ->
                                if (failure.domainError() is DomainError.QueryFailed) transientFailure = true
                                val error = failure.domainError() ?: DomainError.QueryFailed(failure.message)
                                snapshotRepository.recordError(appWidgetId, error, now)
                                instrumentation.failed(failure)
                            }
                    }
                if (unexpected != null) {
                    transientFailure = true
                    val detail = unexpected.message ?: unexpected::class.simpleName
                    snapshotRepository.recordError(appWidgetId, DomainError.QueryFailed(detail), now)
                    instrumentation.unexpected(unexpected)
                }
            }

            guarded { AgendaGlanceWidget().updateAll(applicationContext) }?.let(instrumentation::unexpected)

            val fallbackMidnight = today.plusDays(1).atStartOfDay(zone).toInstant()
            scheduler.schedule(earliestBoundary ?: fallbackMidnight)

            instrumentation.completed(Duration.between(now, clock.instant()), glanceIds.size)

            val retrying = transientFailure && runAttemptCount < MAX_RETRIES
            // Requests that arrived mid-run were dropped by KEEP; chain one
            // more pass so they aren't lost until the next external trigger.
            if (!retrying && refresher.generation() != generationAtStart) refresher.enqueueFollowUp()

            return if (retrying) Result.retry() else Result.success()
        }

        /** Contains one step's failure so the rest of the pipeline still runs; returns the catch, if any. */
        @Suppress("TooGenericExceptionCaught")
        private suspend fun guarded(block: suspend () -> Unit): Exception? =
            try {
                block()
                null
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (unexpected: Exception) {
                unexpected
            }

        private companion object {
            const val MAX_RETRIES = 3
        }
    }
