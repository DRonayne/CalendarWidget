package com.darach.calendarwidget.feature.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.calendarwidget.core.common.analytics.Analytics
import com.darach.calendarwidget.core.common.analytics.AnalyticsEvent
import com.darach.calendarwidget.core.common.crash.CrashReporter
import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import com.darach.calendarwidget.core.model.WidgetConfig
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/** appWidgetId 0 (INVALID_APPWIDGET_ID) edits the global template. */
@HiltViewModel(assistedFactory = ConfigureViewModel.Factory::class)
class ConfigureViewModel
    @AssistedInject
    constructor(
        @Assisted private val appWidgetId: Int,
        private val configRepository: WidgetConfigRepository,
        private val refresher: WidgetRefresher,
        private val analytics: Analytics,
        private val crashReporter: CrashReporter,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(appWidgetId: Int): ConfigureViewModel
        }

        private val isInstance = appWidgetId != 0

        private val _uiState = MutableStateFlow(ConfigureUiState(isInstance = isInstance))
        val uiState: StateFlow<ConfigureUiState> = _uiState.asStateFlow()

        private val _effects = Channel<ConfigureEffect>(Channel.BUFFERED)
        val effects = _effects.receiveAsFlow()

        init {
            viewModelScope.launch {
                val store = configRepository.current()
                _uiState.update { it.copy(loading = false, config = store.configFor(appWidgetId)) }
            }
        }

        private var applyJob: Job? = null

        fun onEvent(event: ConfigureEvent) {
            when (event) {
                is ConfigureEvent.ConfigChanged -> {
                    _uiState.update { it.copy(config = event.config) }
                    applyLive(event.config)
                }

                ConfigureEvent.CalendarsClicked -> {
                    send(ConfigureEffect.OpenCalendars(appWidgetId))
                }

                ConfigureEvent.SaveClicked -> {
                    save()
                }
            }
        }

        /** Every adjustment hits the widget immediately; debounced so slider drags coalesce. */
        private fun applyLive(config: WidgetConfig) {
            applyJob?.cancel()
            applyJob =
                viewModelScope.launch {
                    delay(APPLY_DEBOUNCE_MS)
                    persist(config)
                    refresher.requestRefresh(RefreshReason.CONFIG_CHANGED)
                }
        }

        private suspend fun persist(config: WidgetConfig) {
            if (isInstance) {
                configRepository.setFor(appWidgetId, config)
            } else {
                configRepository.updateGlobal { config }
            }
        }

        /**
         * Waits for the widget to actually finish refreshing before leaving the screen, so the
         * user doesn't land back on a stale/loading widget — bounded so a slow or failing
         * refresh can't strand them on the save button forever. Saving an instance always also
         * updates the global template, so there's one save action and it always sticks as the
         * default for new widgets too.
         */
        private fun save() {
            applyJob?.cancel()
            viewModelScope.launch {
                _uiState.update { it.copy(saving = true) }
                val config = _uiState.value.config
                persist(config)
                if (isInstance) {
                    configRepository.updateGlobal { config }
                }
                val refreshed =
                    withTimeoutOrNull(REFRESH_TIMEOUT_MS) {
                        refresher.refreshAndAwait(RefreshReason.CONFIG_CHANGED)
                    }
                if (refreshed == null) {
                    crashReporter.log("config save: refresh await timed out")
                    analytics.track(AnalyticsEvent.ConfigSaveTimedOut)
                }
                analytics.track(config.toSavedEvent(isInstance))
                _uiState.update { it.copy(saving = false) }
                _effects.send(
                    if (isInstance) ConfigureEffect.FinishWithResult(appWidgetId) else ConfigureEffect.CloseApp,
                )
            }
        }

        private fun send(effect: ConfigureEffect) {
            viewModelScope.launch { _effects.send(effect) }
        }

        private companion object {
            const val APPLY_DEBOUNCE_MS = 400L
            const val REFRESH_TIMEOUT_MS = 8_000L
        }
    }

/** Settings values and a hidden-calendar count only — never calendar identity. */
private fun WidgetConfig.toSavedEvent(isInstance: Boolean) =
    AnalyticsEvent.ConfigSaved(
        daysAhead = daysAhead,
        includeYesterday = includeYesterday,
        emptyDayBehavior = emptyDayBehavior.name,
        textScale = textScale.name,
        hideDeclined = hideDeclined,
        hiddenCalendarCount = hiddenCalendarIds.size,
        showAttendeePhotos = showAttendeePhotos,
        showAddButton = showAddButton,
        showEndTime = showEndTime,
        showDurationChip = showDurationChip,
        isInstance = isInstance,
    )

@Immutable
data class ConfigureUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val isInstance: Boolean = false,
    val config: WidgetConfig = WidgetConfig(),
)

sealed interface ConfigureEvent {
    data class ConfigChanged(
        val config: WidgetConfig,
    ) : ConfigureEvent

    data object CalendarsClicked : ConfigureEvent

    data object SaveClicked : ConfigureEvent
}

sealed interface ConfigureEffect {
    data class OpenCalendars(
        val appWidgetId: Int,
    ) : ConfigureEffect

    data class FinishWithResult(
        val appWidgetId: Int,
    ) : ConfigureEffect

    /** Global template saved; close the app so the user sees their widget update. */
    data object CloseApp : ConfigureEffect
}
