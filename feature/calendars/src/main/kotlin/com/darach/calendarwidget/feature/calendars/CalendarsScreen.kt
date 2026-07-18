package com.darach.calendarwidget.feature.calendars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.darach.calendarwidget.core.designsystem.theme.CalendarWidgetTheme
import com.darach.calendarwidget.core.model.DomainError
import kotlinx.collections.immutable.persistentListOf

@Composable
fun CalendarsScreen(
    appWidgetId: Int,
    modifier: Modifier = Modifier,
) {
    val viewModel =
        hiltViewModel<CalendarsViewModel, CalendarsViewModel.Factory>(
            key = "calendars-$appWidgetId",
        ) { factory -> factory.create(appWidgetId) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    CalendarsContent(state = state, onToggle = viewModel::toggle, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarsContent(
    state: CalendarsUiState,
    onToggle: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Calendars") }) },
    ) { padding ->
        when {
            state.loading -> {
                CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
            }

            state.error != null -> {
                ErrorState(state.error, Modifier.padding(padding))
            }

            else -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(state.rows, key = CalendarRow::id) { row ->
                        CalendarRowItem(row = row, onToggle = onToggle)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarRowItem(
    row: CalendarRow,
    onToggle: (Long, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(14.dp)
                    .background(Color(row.color).copy(alpha = 1f), CircleShape),
        )
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(row.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                row.account,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Checkbox(checked = row.visible, onCheckedChange = { onToggle(row.id, it) })
    }
}

@Composable
private fun ErrorState(
    error: DomainError,
    modifier: Modifier = Modifier,
) {
    val message =
        when (error) {
            DomainError.PermissionMissing -> "Calendar permission is required to list calendars."
            DomainError.ProviderUnavailable -> "Calendar provider unavailable."
            is DomainError.QueryFailed -> "Could not load calendars."
        }
    Text(message, modifier = modifier.padding(24.dp))
}

@Preview(name = "Calendars", showSystemUi = true)
@Composable
private fun CalendarsContentPreview() {
    CalendarWidgetTheme {
        CalendarsContent(
            state =
                CalendarsUiState(
                    loading = false,
                    rows =
                        persistentListOf(
                            CalendarRow(1, "Personal", "local", 0xFF33AA88.toInt(), true),
                            CalendarRow(2, "Work", "work@example.com", 0xFF3366CC.toInt(), false),
                            CalendarRow(3, "Family", "shared@example.com", 0xFFE07A30.toInt(), true),
                            CalendarRow(4, "Birthdays", "contacts", 0xFF9C4DCC.toInt(), true),
                            CalendarRow(5, "Holidays in Ireland", "holiday@group", 0xFF2E7D32.toInt(), false),
                            CalendarRow(6, "Sport fixtures", "sport@group", 0xFF1565C0.toInt(), true),
                        ),
                ),
            onToggle = { _, _ -> },
        )
    }
}

@Preview(name = "Calendars error", showSystemUi = true)
@Composable
private fun CalendarsErrorPreview() {
    CalendarWidgetTheme {
        CalendarsContent(
            state = CalendarsUiState(loading = false, error = DomainError.PermissionMissing),
            onToggle = { _, _ -> },
        )
    }
}
