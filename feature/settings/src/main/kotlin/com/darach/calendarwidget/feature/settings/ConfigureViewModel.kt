package com.darach.calendarwidget.feature.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.calendarwidget.core.common.analytics.Analytics
import com.darach.calendarwidget.core.common.analytics.AnalyticsEvent
import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import com.darach.calendarwidget.core.model.WidgetConfig
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** appWidgetId 0 (INVALID_APPWIDGET_ID) edits the global template. */
@HiltViewModel(assistedFactory = ConfigureViewModel.Factory::class)
class ConfigureViewModel
    @AssistedInject
    constructor(
        @Assisted private val appWidgetId: Int,
        private val configRepository: WidgetConfigRepository,
        private val refresher: WidgetRefresher,
        private val analytics: Analytics,
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

        fun onEvent(event: ConfigureEvent) {
            when (event) {
                is ConfigureEvent.ConfigChanged -> _uiState.update { it.copy(config = event.config) }
                ConfigureEvent.CalendarsClicked -> send(ConfigureEffect.OpenCalendars(appWidgetId))
                ConfigureEvent.AboutClicked -> send(ConfigureEffect.OpenAbout)
                ConfigureEvent.SaveClicked -> save(alsoAsTemplate = false)
                ConfigureEvent.SaveAsDefaultClicked -> save(alsoAsTemplate = true)
            }
        }

        private fun save(alsoAsTemplate: Boolean) {
            viewModelScope.launch {
                val config = _uiState.value.config
                if (isInstance) {
                    configRepository.setFor(appWidgetId, config)
                }
                if (!isInstance || alsoAsTemplate) {
                    configRepository.updateGlobal { config }
                }
                refresher.requestRefresh(RefreshReason.CONFIG_CHANGED)
                analytics.track(AnalyticsEvent.ConfigSaved)
                _effects.send(
                    if (isInstance) ConfigureEffect.FinishWithResult(appWidgetId) else ConfigureEffect.CloseApp,
                )
            }
        }

        private fun send(effect: ConfigureEffect) {
            viewModelScope.launch { _effects.send(effect) }
        }
    }

@Immutable
data class ConfigureUiState(
    val loading: Boolean = true,
    val isInstance: Boolean = false,
    val config: WidgetConfig = WidgetConfig(),
)

sealed interface ConfigureEvent {
    data class ConfigChanged(
        val config: WidgetConfig,
    ) : ConfigureEvent

    data object CalendarsClicked : ConfigureEvent

    data object AboutClicked : ConfigureEvent

    data object SaveClicked : ConfigureEvent

    data object SaveAsDefaultClicked : ConfigureEvent
}

sealed interface ConfigureEffect {
    data class OpenCalendars(
        val appWidgetId: Int,
    ) : ConfigureEffect

    data object OpenAbout : ConfigureEffect

    data class FinishWithResult(
        val appWidgetId: Int,
    ) : ConfigureEffect

    /** Global template saved; close the app so the user sees their widget update. */
    data object CloseApp : ConfigureEffect
}
