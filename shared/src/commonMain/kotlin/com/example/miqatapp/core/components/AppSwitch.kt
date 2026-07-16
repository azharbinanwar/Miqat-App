package com.example.miqatapp.core.components

import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

/** The app's switch: an M3 Switch trimmed ~15% so it sits lighter next to text. Touch target is unchanged
 *  (scale is draw-only), so rows still meet the 48dp minimum. Drop-in for Material's Switch. */
@Composable
fun AppSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled, modifier = modifier.scale(0.85f))
}
