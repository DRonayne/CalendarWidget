package com.darach.calendarwidget.feature.settings

import com.darach.calendarwidget.core.common.analytics.Analytics
import com.darach.calendarwidget.core.common.analytics.AnalyticsEvent
import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import com.darach.calendarwidget.core.model.WidgetConfig
import com.darach.calendarwidget.core.model.WidgetConfigStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FakeWidgetConfigRepository(
    initial: WidgetConfigStore = WidgetConfigStore(),
) : WidgetConfigRepository {
    private val state = MutableStateFlow(initial)

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
}

class FakeWidgetRefresher : WidgetRefresher {
    val requests = mutableListOf<RefreshReason>()

    override fun requestRefresh(reason: RefreshReason) {
        requests += reason
    }
}

class RecordingAnalytics : Analytics {
    val events = mutableListOf<AnalyticsEvent>()

    override fun track(event: AnalyticsEvent) {
        events += event
    }
}
