package com.darach.calendarwidget.core.data.config

import androidx.datastore.core.DataStore
import com.darach.calendarwidget.core.model.WidgetConfig
import com.darach.calendarwidget.core.model.WidgetConfigStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/** Per-widget configuration with global-template fallback. */
interface WidgetConfigRepository {
    val store: Flow<WidgetConfigStore>

    fun configFor(appWidgetId: Int): Flow<WidgetConfig>

    suspend fun current(): WidgetConfigStore

    suspend fun updateGlobal(transform: (WidgetConfig) -> WidgetConfig)

    suspend fun setFor(
        appWidgetId: Int,
        config: WidgetConfig,
    )

    /** Drops config for deleted widget instances. */
    suspend fun remove(appWidgetIds: Collection<Int>)
}

@Singleton
class WidgetConfigRepositoryImpl
    @Inject
    constructor(
        @param:Named("widgetConfig") private val dataStore: DataStore<WidgetConfigStore>,
    ) : WidgetConfigRepository {
        override val store: Flow<WidgetConfigStore> = dataStore.data

        override fun configFor(appWidgetId: Int): Flow<WidgetConfig> =
            dataStore.data.map { it.configFor(appWidgetId) }.distinctUntilChanged()

        override suspend fun current(): WidgetConfigStore = dataStore.data.first()

        override suspend fun updateGlobal(transform: (WidgetConfig) -> WidgetConfig) {
            dataStore.updateData { it.copy(global = transform(it.global)) }
        }

        override suspend fun setFor(
            appWidgetId: Int,
            config: WidgetConfig,
        ) {
            dataStore.updateData { it.copy(byWidgetId = it.byWidgetId + (appWidgetId to config)) }
        }

        override suspend fun remove(appWidgetIds: Collection<Int>) {
            dataStore.updateData { it.copy(byWidgetId = it.byWidgetId - appWidgetIds.toSet()) }
        }
    }
