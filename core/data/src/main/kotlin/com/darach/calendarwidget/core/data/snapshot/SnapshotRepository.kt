package com.darach.calendarwidget.core.data.snapshot

import androidx.datastore.core.DataStore
import com.darach.calendarwidget.core.model.AgendaSnapshot
import com.darach.calendarwidget.core.model.DomainError
import com.darach.calendarwidget.core.model.SnapshotStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Last rendered agenda per widget instance — the stale-then-fresh accelerator.
 * The calendar provider stays the source of truth; this is never authoritative.
 */
interface SnapshotRepository {
    val store: Flow<SnapshotStore>

    fun snapshotFor(appWidgetId: Int): Flow<AgendaSnapshot?>

    suspend fun current(): SnapshotStore

    suspend fun put(
        appWidgetId: Int,
        snapshot: AgendaSnapshot,
    )

    /**
     * Records why the most recent refresh failed, preserving any previously
     * rendered days so a stale-but-visible agenda isn't wiped out by a
     * transient failure.
     */
    suspend fun recordError(
        appWidgetId: Int,
        error: DomainError,
        now: Instant,
    )

    suspend fun remove(appWidgetIds: Collection<Int>)
}

@Singleton
class SnapshotRepositoryImpl
    @Inject
    constructor(
        @param:Named("agendaSnapshot") private val dataStore: DataStore<SnapshotStore>,
    ) : SnapshotRepository {
        override val store: Flow<SnapshotStore> = dataStore.data

        override fun snapshotFor(appWidgetId: Int): Flow<AgendaSnapshot?> =
            dataStore.data.map { it.byWidgetId[appWidgetId] }.distinctUntilChanged()

        override suspend fun current(): SnapshotStore = dataStore.data.first()

        override suspend fun put(
            appWidgetId: Int,
            snapshot: AgendaSnapshot,
        ) {
            dataStore.updateData { it.copy(byWidgetId = it.byWidgetId + (appWidgetId to snapshot)) }
        }

        override suspend fun recordError(
            appWidgetId: Int,
            error: DomainError,
            now: Instant,
        ) {
            dataStore.updateData { store ->
                val existing = store.byWidgetId[appWidgetId]
                val updated =
                    existing?.copy(lastError = error)
                        ?: AgendaSnapshot(generatedAt = now, days = emptyList(), lastError = error)
                store.copy(byWidgetId = store.byWidgetId + (appWidgetId to updated))
            }
        }

        override suspend fun remove(appWidgetIds: Collection<Int>) {
            dataStore.updateData { it.copy(byWidgetId = it.byWidgetId - appWidgetIds.toSet()) }
        }
    }
