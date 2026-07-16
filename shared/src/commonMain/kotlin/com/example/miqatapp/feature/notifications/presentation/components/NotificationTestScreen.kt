package com.example.miqatapp.feature.notifications.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.components.MiniStepper
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.datetime.currentTime
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.feature.notifications.scheduler.NotificationScheduler
import com.example.miqatapp.feature.notifications.store.NotificationTestStore
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

// Dev screen (7-tap): see everything scheduled and add one-shots. A full screen (not a sheet) so it won't
// dismiss on a stray tap, and the Add button stays pinned no matter how long the list gets.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationTestScreen(onBack: () -> Unit) {
    val c = AppTheme.colors
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val saved by NotificationTestStore.items.collectAsState()
    val scheduled by NotificationScheduler.scheduled().collectAsState(emptyList())
    val pattern = timeFormat.pattern
    val tz = TimeZone.currentSystemDefault()
    var fireIn by remember { mutableStateOf(1) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Notification test") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), "Back") } },
            )
        },
        bottomBar = {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Fire in", color = c.onSurface)
                    MiniStepper(fireIn, "min", { fireIn = it }, min = 1, max = 120)
                }
                AppButton(text = "Add ($fireIn min)", onClick = { val (at, label) = slotFor(fireIn, pattern); NotificationTestStore.add(at, label) }, modifier = Modifier.fillMaxWidth())
            }
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            if (saved.isNotEmpty()) {
                Text("Scheduled test alerts", fontSize = 12.sp, color = c.onSurfaceVariant)
                saved.forEach { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(18.dp))
                            Text("Test #${item.id} · fires at ${item.label}", color = c.onSurface)
                        }
                        Text("Remove", fontSize = 13.sp, color = c.primary, modifier = Modifier.clickable { NotificationTestStore.remove(item) })
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Text("All scheduled (${scheduled.size})", fontSize = 12.sp, color = c.onSurfaceVariant)
            scheduled.forEach { e ->
                val t = Instant.fromEpochMilliseconds(e.fireAtMillis).toLocalDateTime(tz).time.format(pattern)
                val name = if (e.target == "test") "Test #${e.eventKey.substringAfterLast(':')}" else e.target
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("#${e.slotId} · $name · ${e.kind}", color = c.onSurface)
                    Text(t, fontSize = 13.sp, color = c.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun slotFor(fireIn: Int, pattern: String): Pair<Long, String> {
    val tz = TimeZone.currentSystemDefault()
    val now = LocalDateTime(currentDate(), currentTime()).toInstant(tz).toEpochMilliseconds()
    val base = currentTime()
    val total = (base.hour * 60 + base.minute + fireIn) % 1440
    return (now + fireIn * 60_000L) to LocalTime(total / 60, total % 60).format(pattern)
}
