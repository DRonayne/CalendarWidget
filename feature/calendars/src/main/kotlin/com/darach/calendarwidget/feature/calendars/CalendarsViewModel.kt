package com.darach.calendarwidget.feature.calendars

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.calendarwidget.core.common.crash.CrashReporter
import com.darach.calendarwidget.core.data.config.WidgetConfigRepository
import com.darach.calendarwidget.core.data.refresh.RefreshReason
import com.darach.calendarwidget.core.data.refresh.WidgetRefresher
import com.darach.calendarwidget.core.data.repository.CalendarRepository
import com.darach.calendarwidget.core.model.DomainError
import com.darach.calendarwidget.core.model.domainError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Calendar visibility for one widget instance (appWidgetId 0 = global template).
 * Toggles persist immediately; the widget refresh piggybacks on CONFIG_CHANGED.
 */
@HiltViewModel(assistedFactory = CalendarsViewModel.Factory::class)
class CalendarsViewModel
    @AssistedInject
    constructor(
        @Assisted private val appWidgetId: Int,
        private val calendarRepository: CalendarRepository,
        private val configRepository: WidgetConfigRepository,
        private val refresher: WidgetRefresher,
        private val crashReporter: CrashReporter,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(appWidgetId: Int): CalendarsViewModel
        }

        private val _uiState = MutableStateFlow(CalendarsUiState())
        val uiState: StateFlow<CalendarsUiState> = _uiState.asStateFlow()

        init {
            load()
        }

        fun toggle(
            calendarId: Long,
            visible: Boolean,
        ) {
            viewModelScope.launch {
                val hidden =
                    _uiState.value.rows
                        .map { if (it.id == calendarId) it.copy(visible = visible) else it }
                        .also { rows -> _uiState.update { it.copy(rows = rows.toImmutableList()) } }
                        .filterNot(CalendarRow::visible)
                        .map(CalendarRow::id)
                        .toSet()
                if (appWidgetId != 0) {
                    val config = configRepository.current().configFor(appWidgetId)
                    configRepository.setFor(appWidgetId, config.copy(hiddenCalendarIds = hidden))
                } else {
                    configRepository.updateGlobal { it.copy(hiddenCalendarIds = hidden) }
                }
                refresher.requestRefresh(RefreshReason.CONFIG_CHANGED)
            }
        }

        private fun load() {
            viewModelScope.launch {
                val hidden = configRepository.current().configFor(appWidgetId).hiddenCalendarIds
                calendarRepository
                    .calendars()
                    .onSuccess { calendars ->
                        val rows =
                            calendars.map {
                                CalendarRow(
                                    id = it.id,
                                    name = it.displayName,
                                    account = it.accountName,
                                    color = it.color,
                                    visible = it.id !in hidden,
                                )
                            }
                        _uiState.update { it.copy(loading = false, rows = rows.toImmutableList()) }
                    }.onFailure { failure ->
                        // Missing permission is an expected pre-grant state, not a defect.
                        if (failure.domainError() != DomainError.PermissionMissing) {
                            crashReporter.recordNonFatal(failure)
                        }
                        _uiState.update { it.copy(loading = false, error = failure.domainError()) }
                    }
            }
        }
    }

@Immutable
data class CalendarsUiState(
    val loading: Boolean = true,
    val rows: ImmutableList<CalendarRow> = persistentListOf(),
    val error: DomainError? = null,
)

data class CalendarRow(
    val id: Long,
    val name: String,
    val account: String,
    val color: Int,
    val visible: Boolean,
)
