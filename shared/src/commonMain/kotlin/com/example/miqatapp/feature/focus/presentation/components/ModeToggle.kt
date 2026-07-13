package com.example.miqatapp.feature.focus.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.focus.SilenceMode

// One connected segmented toggle: Silent | Vibrate. The selected half fills with primary.
@Composable
fun ModeToggle(mode: SilenceMode, onPick: (SilenceMode) -> Unit) {
    val c = AppTheme.colors
    Row(Modifier.clip(RoundedCornerShape(10.dp)).background(c.surfaceContainerHigh).padding(2.dp)) {
        SilenceMode.entries.forEach { m ->
            val sel = m == mode
            Row(
                Modifier.clip(RoundedCornerShape(8.dp))
                    .background(if (sel) c.primary else Color.Transparent)
                    .clickable { onPick(m) }
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(m.icon, null, tint = if (sel) c.onPrimary else c.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text(m.label(), fontSize = 12.sp, color = if (sel) c.onPrimary else c.onSurfaceVariant)
            }
        }
    }
}
