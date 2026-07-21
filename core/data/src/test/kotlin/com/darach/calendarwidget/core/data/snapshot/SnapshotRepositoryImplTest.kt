package com.darach.calendarwidget.core.data.snapshot

import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.darach.calendarwidget.core.data.datastore.JsonDataStoreSerializer
import com.darach.calendarwidget.core.model.AgendaDay
import com.darach.calendarwidget.core.model.AgendaSnapshot
import com.darach.calendarwidget.core.model.DomainError
import com.darach.calendarwidget.core.model.SnapshotStore
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.Instant
import java.time.LocalDate

class SnapshotRepositoryImplTest {
    @TempDir
    lateinit var tempDir: File

    private fun repository(): SnapshotRepository =
        SnapshotRepositoryImpl(
            DataStoreFactory.create(
                serializer = JsonDataStoreSerializer(SnapshotStore.serializer(), SnapshotStore()),
                corruptionHandler = ReplaceFileCorruptionHandler { SnapshotStore() },
                produceFile = { File(tempDir, "agenda_snapshot.json") },
            ),
        )

    @Test
    fun `recordError creates a placeholder snapshot when none exists`() =
        runTest {
            val repo = repository()
            val now = Instant.parse("2026-07-20T08:00:00Z")

            repo.recordError(12, DomainError.ProviderUnavailable, now)

            val snapshot = repo.current().byWidgetId[12]
            assertEquals(DomainError.ProviderUnavailable, snapshot?.lastError)
            assertEquals(emptyList<AgendaDay>(), snapshot?.days)
        }

    @Test
    fun `recordError preserves previously stored days`() =
        runTest {
            val repo = repository()
            val now = Instant.parse("2026-07-20T08:00:00Z")
            val day = AgendaDay(LocalDate.of(2026, 7, 20), emptyList())
            repo.put(12, AgendaSnapshot(generatedAt = now, days = listOf(day)))

            repo.recordError(12, DomainError.QueryFailed("boom"), now.plusSeconds(60))

            val snapshot = repo.current().byWidgetId[12]
            assertEquals(listOf(day), snapshot?.days)
            assertEquals(DomainError.QueryFailed("boom"), snapshot?.lastError)
        }

    @Test
    fun `put clears a previously recorded error`() =
        runTest {
            val repo = repository()
            val now = Instant.parse("2026-07-20T08:00:00Z")
            repo.recordError(12, DomainError.ProviderUnavailable, now)

            repo.put(12, AgendaSnapshot(generatedAt = now, days = emptyList()))

            assertNull(repo.current().byWidgetId[12]?.lastError)
        }
}
