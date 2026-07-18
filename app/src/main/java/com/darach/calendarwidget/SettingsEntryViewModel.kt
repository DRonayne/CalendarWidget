package com.darach.calendarwidget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darach.calendarwidget.core.data.refresh.PlacedWidgets
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Opening the app from the launcher icon (not a widget's own "reconfigure") is
 * ambiguous about which config to edit: the global template only affects widgets
 * placed *after* the edit, since every placed widget gets its own override the
 * moment it's first configured. With exactly one widget already on the home
 * screen, editing "the template" silently does nothing the user can see — so
 * resolve to that widget's own config instead. Falls back to the template when
 * there's zero or more than one, where a single target is ambiguous.
 */
@HiltViewModel
class SettingsEntryViewModel
    @Inject
    constructor(
        placedWidgets: PlacedWidgets,
    ) : ViewModel() {
        private val _targetAppWidgetId = MutableStateFlow<Int?>(null)
        val targetAppWidgetId: StateFlow<Int?> = _targetAppWidgetId.asStateFlow()

        init {
            viewModelScope.launch {
                _targetAppWidgetId.value = placedWidgets.ids().singleOrNull() ?: 0
            }
        }
    }
