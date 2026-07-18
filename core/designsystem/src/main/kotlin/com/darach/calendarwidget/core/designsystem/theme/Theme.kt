package com.darach.calendarwidget.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Thin brand layer over Material 3. Light + dark only — dynamic color is
// deliberately reserved for the widget (which must blend with the wallpaper).
private val BrandLight =
    lightColorScheme(
        primary = Color(0xFF006B5F),
        secondary = Color(0xFF4A635E),
        tertiary = Color(0xFF456179),
    )

private val BrandDark =
    darkColorScheme(
        primary = Color(0xFF54DBC8),
        secondary = Color(0xFFB1CCC4),
        tertiary = Color(0xFFADCAE5),
    )

@Composable
fun CalendarWidgetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) BrandDark else BrandLight,
        content = content,
    )
}
