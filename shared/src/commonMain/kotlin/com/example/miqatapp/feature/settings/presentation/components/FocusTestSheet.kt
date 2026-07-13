package com.example.miqatapp.feature.settings.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.components.MiniStepper
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.datetime.currentTime
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.focus.SilenceMode
import com.example.miqatapp.core.focus.rememberFocusSetup
import com.example.miqatapp.core.store.FocusTestStore
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.core.store.TestSlot
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

// Test tool: add one-shot mute slots that are saved (so they survive a reboot) and fire once.
@Composable
fun FocusTestSheet(onDismiss: () -> Unit) {
    val c = AppTheme.colors
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val saved by FocusTestStore.slots.collectAsState()

    var startIn by remember { mutableStateOf(2) } // minutes from now until the slot starts
    var duration by remember { mutableStateOf(2) }
    var mode by remember { mutableStateOf(SilenceMode.Vibrate) }
    val setup = rememberFocusSetup()

    AppBottomSheet(
        onDismiss = onDismiss,
        title = "Background test",
        subtitle = "Saved slots survive a reboot and fire once. Add one, then kill the app or restart.",
        footer = {
            AppButton(
                text = "Add slot",
                onClick = { FocusTestStore.add(slotFor(startIn, duration, mode, timeFormat.pattern)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Start in", color = c.onSurface)
            MiniStepper(startIn, "min", { startIn = it }, min = 1, max = 120)
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Silence for", color = c.onSurface)
            MiniStepper(duration, "min", { duration = it }, min = 1, max = 60)
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Silence mode", color = c.onSurface)
            ModeToggle(mode) { m ->
                if (m == SilenceMode.Silent && !setup.hasSilenceAccess()) setup.requestSilenceAccess()
                mode = m
            }
        }
        if (saved.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Scheduled", fontSize = 12.sp, color = c.onSurfaceVariant)
        }
        saved.forEach { slot ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${slot.label} · ${slot.mode}", color = c.onSurface)
                Text("Remove", fontSize = 13.sp, color = c.primary, modifier = Modifier.clickable { FocusTestStore.remove(slot) })
            }
        }
    }
}

// "5:04 for 2 min" + absolute epoch millis, from "start in [startIn] min" and a duration.
private fun slotFor(startIn: Int, duration: Int, mode: SilenceMode, pattern: String): TestSlot {
    val tz = TimeZone.currentSystemDefault()
    val nowMillis = LocalDateTime(currentDate(), currentTime()).toInstant(tz).toEpochMilliseconds()
    val start = nowMillis + startIn * 60_000L
    val end = start + duration * 60_000L
    val base = currentTime()
    val total = (base.hour * 60 + base.minute + startIn) % 1440
    val label = "${LocalTime(total / 60, total % 60).format(pattern)} for $duration min"
    return TestSlot(start, end, mode.name, label)
}
