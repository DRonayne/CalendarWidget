package com.darach.calendarwidget.feature.calendars

import com.darach.calendarwidget.core.common.crash.NoOpCrashReporter
import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import com.darach.calendarwidget.core.data.repository.CalendarRepository
import com.darach.calendarwidget.core.domain.AgendaWindow
import com.darach.calendarwidget.core.model.CalendarEvent
import com.darach.calendarwidget.core.model.CalendarInfo
import com.darach.calendarwidget.core.model.DomainError
import com.darach.calendarwidget.core.model.DomainException
import com.darach.calendarwidget.core.model.WidgetConfig
import com.darach.calendarwidget.core.model.WidgetConfigStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var configs: FakeConfigRepository
    private lateinit var calendars: FakeCalendarRepository
    private lateinit var refresher: FakeRefresher

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        configs = FakeConfigRepository()
        calendars =
            FakeCalendarRepository(
                listOf(
                    CalendarInfo(1, "Personal", "local", 0xFF0000),
                    CalendarInfo(2, "Work", "work@x.com", 0x00FF00),
                ),
            )
        refresher = FakeRefresher()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(id: Int = 7) = CalendarsViewModel(id, calendars, configs, refresher, NoOpCrashReporter)

    @Test
    fun `loads calendars with visibility from config`() =
        runTest(dispatcher) {
            configs.set(7, WidgetConfig(hiddenCalendarIds = setOf(2L)))
            val vm = viewModel()
            dispatcher.scheduler.advanceUntilIdle()

            val rows = vm.uiState.value.rows
            assertEquals(2, rows.size)
            assertTrue(rows.first { it.id == 1L }.visible)
            assertFalse(rows.first { it.id == 2L }.visible)
        }

    @Test
    fun `toggling hides calendar and requests refresh`() =
        runTest(dispatcher) {
            val vm = viewModel()
            dispatcher.scheduler.advanceUntilIdle()

            vm.toggle(1L, visible = false)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(setOf(1L), configs.current().configFor(7).hiddenCalendarIds)
            assertEquals(listOf(RefreshReason.CONFIG_CHANGED), refresher.requests)
            assertFalse(
                vm.uiState.value.rows
                    .first { it.id == 1L }
                    .visible,
            )
        }

    @Test
    fun `permission failure surfaces as error state`() =
        runTest(dispatcher) {
            calendars.failWith = DomainException(DomainError.PermissionMissing)
            val vm = viewModel()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(DomainError.PermissionMissing, vm.uiState.value.error)
        }
}

private class FakeConfigRepository : WidgetConfigRepository {
    private val state = MutableStateFlow(WidgetConfigStore())

    override val store: Flow<WidgetConfigStore> = state

    override fun configFor(appWidgetId: Int): Flow<WidgetConfig> =
        state.map { it.configFor(appWidgetId) }.distinctUntilChanged()

    override suspend fun current(): WidgetConfigStore = state.value

    override suspend fun updateGlobal(transform: (WidgetConfig) -> WidgetConfig) {
        state.value = state.value.copy(global = transform(state.value.global))
    }

    override suspend fun setFor(
        appWidgetId: Int,
        config: WidgetConfig,
    ) {
        state.value = state.value.copy(byWidgetId = state.value.byWidgetId + (appWidgetId to config))
    }

    override suspend fun remove(appWidgetIds: Collection<Int>) {
        state.value = state.value.copy(byWidgetId = state.value.byWidgetId - appWidgetIds.toSet())
    }

    fun set(
        appWidgetId: Int,
        config: WidgetConfig,
    ) {
        state.value = state.value.copy(byWidgetId = state.value.byWidgetId + (appWidgetId to config))
    }
}

private class FakeCalendarRepository(
    private val calendars: List<CalendarInfo>,
) : CalendarRepository {
    var failWith: DomainException? = null

    override suspend fun events(
        window: AgendaWindow,
        zone: ZoneId,
        hiddenCalendarIds: Set<Long>,
        hideDeclined: Boolean,
        includeAttendeePhotos: Boolean,
    ): Result<List<CalendarEvent>> = Result.success(emptyList())

    override suspend fun calendars(): Result<List<CalendarInfo>> =
        failWith?.let { Result.failure(it) } ?: Result.success(calendars)
}

private class FakeRefresher : WidgetRefresher {
    val requests = mutableListOf<RefreshReason>()

    override fun requestRefresh(reason: RefreshReason) {
        requests += reason
    }

    override suspend fun refreshAndAwait(reason: RefreshReason): Boolean {
        requests += reason
        return true
    }
}
