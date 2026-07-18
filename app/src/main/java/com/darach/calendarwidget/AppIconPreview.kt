package com.darach.calendarwidget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Design-time only: the launcher icon under the common mask shapes. */
@Preview(name = "App icon", showBackground = true, backgroundColor = 0xFFFAF8FF)
@Composable
private fun AppIconPreview() {
    Row(
        modifier = Modifier.padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        MaskedIcon(CircleShape)
        MaskedIcon(RoundedCornerShape(24.dp))
    }
}

@Composable
private fun MaskedIcon(shape: Shape) {
    Box(
        modifier =
            Modifier
                .size(96.dp)
                .clip(shape)
                .background(Color(0xFF334478)),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
        )
    }
}
