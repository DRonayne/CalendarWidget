package com.darach.calendarwidget.feature.settings

import app.cash.turbine.test
import com.darach.calendarwidget.core.common.analytics.AnalyticsEvent
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.model.WidgetConfig
import com.darach.calendarwidget.core.model.WidgetConfigStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigureViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var configs: FakeWidgetConfigRepository
    private lateinit var refresher: FakeWidgetRefresher
    private lateinit var analytics: RecordingAnalytics

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        configs =
            FakeWidgetConfigRepository(
                WidgetConfigStore(
                    global = WidgetConfig(daysAhead = 14),
                    byWidgetId = mapOf(7 to WidgetConfig(daysAhead = 3)),
                ),
            )
        refresher = FakeWidgetRefresher()
        analytics = RecordingAnalytics()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(appWidgetId: Int) = ConfigureViewModel(appWidgetId, configs, refresher, analytics)

    @Test
    fun `instance loads its own config`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 7)
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(3, vm.uiState.value.config.daysAhead)
            assertTrue(vm.uiState.value.isInstance)
        }

    @Test
    fun `unknown instance inherits the global template`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 99)
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(14, vm.uiState.value.config.daysAhead)
        }

    @Test
    fun `save for instance persists, refreshes, and finishes with result`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 7)
            dispatcher.scheduler.advanceUntilIdle()

            vm.onEvent(
                ConfigureEvent.ConfigChanged(
                    vm.uiState.value.config
                        .copy(daysAhead = 10),
                ),
            )
            vm.effects.test {
                vm.onEvent(ConfigureEvent.SaveClicked)
                assertEquals(ConfigureEffect.FinishWithResult(7), awaitItem())
            }
            assertEquals(10, configs.current().configFor(7).daysAhead)
            assertEquals(listOf(RefreshReason.CONFIG_CHANGED), refresher.requests)
            assertEquals(listOf<AnalyticsEvent>(AnalyticsEvent.ConfigSaved), analytics.events)
        }

    @Test
    fun `save waits for the refresh to finish before signalling done`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 7)
            dispatcher.scheduler.advanceUntilIdle()
            val gate = CompletableDeferred<Unit>()
            refresher.awaitGate = gate

            vm.effects.test {
                vm.onEvent(ConfigureEvent.SaveClicked)
                // runCurrent (not advanceUntilIdle) so the withTimeoutOrNull delay isn't
                // fast-forwarded through while we're still mid-await on the gate.
                dispatcher.scheduler.runCurrent()
                assertTrue(vm.uiState.value.saving)
                expectNoEvents()

                gate.complete(Unit)
                dispatcher.scheduler.advanceUntilIdle()
                assertEquals(ConfigureEffect.FinishWithResult(7), awaitItem())
            }
            assertFalse(vm.uiState.value.saving)
        }

    @Test
    fun `save as default also updates the global template`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 7)
            dispatcher.scheduler.advanceUntilIdle()

            vm.onEvent(
                ConfigureEvent.ConfigChanged(
                    vm.uiState.value.config
                        .copy(daysAhead = 21),
                ),
            )
            vm.effects.test {
                vm.onEvent(ConfigureEvent.SaveAsDefaultClicked)
                awaitItem()
            }
            assertEquals(21, configs.current().global.daysAhead)
            assertEquals(21, configs.current().configFor(7).daysAhead)
        }

    @Test
    fun `global editor saves template and closes the app`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 0)
            dispatcher.scheduler.advanceUntilIdle()

            vm.onEvent(
                ConfigureEvent.ConfigChanged(
                    vm.uiState.value.config
                        .copy(includeYesterday = true),
                ),
            )
            vm.effects.test {
                vm.onEvent(ConfigureEvent.SaveClicked)
                assertEquals(ConfigureEffect.CloseApp, awaitItem())
            }
            assertTrue(configs.current().global.includeYesterday)
        }

    @Test
    fun `adjusting a setting applies it to the widget after the debounce`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 7)
            dispatcher.scheduler.advanceUntilIdle()

            vm.onEvent(
                ConfigureEvent.ConfigChanged(
                    vm.uiState.value.config
                        .copy(daysAhead = 5),
                ),
            )
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(5, configs.current().configFor(7).daysAhead)
            assertEquals(listOf(RefreshReason.CONFIG_CHANGED), refresher.requests)
        }

    @Test
    fun `rapid adjustments coalesce into one apply with the latest value`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 7)
            dispatcher.scheduler.advanceUntilIdle()

            vm.onEvent(
                ConfigureEvent.ConfigChanged(
                    vm.uiState.value.config
                        .copy(daysAhead = 5),
                ),
            )
            vm.onEvent(
                ConfigureEvent.ConfigChanged(
                    vm.uiState.value.config
                        .copy(daysAhead = 9),
                ),
            )
            vm.onEvent(
                ConfigureEvent.ConfigChanged(
                    vm.uiState.value.config
                        .copy(daysAhead = 21),
                ),
            )
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(21, configs.current().configFor(7).daysAhead)
            assertEquals(listOf(RefreshReason.CONFIG_CHANGED), refresher.requests)
        }

    @Test
    fun `global editor adjustments apply to the template live`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 0)
            dispatcher.scheduler.advanceUntilIdle()

            vm.onEvent(
                ConfigureEvent.ConfigChanged(
                    vm.uiState.value.config
                        .copy(includeYesterday = true),
                ),
            )
            dispatcher.scheduler.advanceUntilIdle()

            assertTrue(configs.current().global.includeYesterday)
            assertEquals(listOf(RefreshReason.CONFIG_CHANGED), refresher.requests)
        }

    @Test
    fun `calendars click emits navigation effect with own id`() =
        runTest(dispatcher) {
            val vm = viewModel(appWidgetId = 7)
            dispatcher.scheduler.advanceUntilIdle()
            vm.effects.test {
                vm.onEvent(ConfigureEvent.CalendarsClicked)
                assertEquals(ConfigureEffect.OpenCalendars(7), awaitItem())
            }
        }
}
