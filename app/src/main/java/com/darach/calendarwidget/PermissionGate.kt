package com.darach.calendarwidget

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.darach.calendarwidget.core.designsystem.theme.CalendarWidgetTheme

/**
 * Blocks the app UI until READ_CALENDAR is granted (the widget has its own
 * grant state). READ_CONTACTS stays optional and is requested alongside —
 * denial only disables attendee avatars.
 */
@Composable
fun PermissionGate(
    onGranted: () -> Unit,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { results ->
            if (results[Manifest.permission.READ_CALENDAR] == true) {
                granted = true
                onGranted()
            }
        }

    if (granted) {
        content()
    } else {
        PermissionRationale(
            onRequest = {
                launcher.launch(
                    arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.READ_CONTACTS),
                )
            },
        )
    }
}

@Composable
private fun PermissionRationale(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Calendar access", style = MaterialTheme.typography.headlineSmall)
        Text(
            "The widget reads your device calendars to show upcoming events. " +
                "Contacts access is optional and only used for attendee photos.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        Button(onClick = onRequest) {
            Text("Grant access")
        }
    }
}

@Preview(name = "Permission gate", showSystemUi = true)
@Composable
private fun PermissionRationalePreview() {
    CalendarWidgetTheme {
        PermissionRationale(onRequest = {})
    }
}
