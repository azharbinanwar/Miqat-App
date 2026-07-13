package com.example.miqatapp.feature.miqat.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Settings
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.datetime.toHijri
import com.example.miqatapp.core.debug.Debug
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.color
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.SehriInfoSheet
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.feature.miqat.store.MiqatCalculationStore
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import com.example.miqatapp.feature.miqat.presentation.components.MonthCalendar
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.prayer_times
import kotlinx.coroutines.launch
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.datetime.format
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource

/** Calendar-first prayer times — today is on Home; here you pick any date. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiqatTimesScreen() {
    val today =
        remember { currentDate() }
    var visible by remember { mutableStateOf(today) } // any day of the visible month
    var selected by remember { mutableStateOf(today) }
    var showSehriInfo by remember { mutableStateOf(false) }

    val place by LocationStore.activePlace.collectAsState()
    val calc by MiqatCalculationStore.calculation.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val times = remember(selected, place, calc) { MiqatTimesStore.timesFor(selected) }
    // Imsak (and any Ramadan-only marker) shows only during Ramadan. Debug.FORCE_RAMADAN previews it off-season.
    val ramadan = remember(selected) { Debug.FORCE_RAMADAN || toHijri(selected).month == 9 }
    val shown = remember(times, ramadan) { times.filter { it.miqat.category != Miqat.Category.RAMADAN || ramadan } }

    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(Res.string.prayer_times)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, "Menu") }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO settings */ }) { Icon(Lucide.Settings, "Settings") }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()),
            ) {
                MonthCalendar(
                    year = visible.year,
                    month = visible.monthNumber,
                    selected = selected,
                    today = today,
                    onSelect = { selected = it },
                    onPrevMonth = { visible = visible.minus(1, DateTimeUnit.MONTH) },
                    onNextMonth = { visible = visible.plus(1, DateTimeUnit.MONTH) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )

                Text(
                    formatSelected(selected),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    fontWeight = FontWeight.SemiBold,
                    color = AppTheme.colors.onSurface,
                )

                AppTileGroup(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    // nest a sub-time under its parent only when the parent sits directly above it in the day
                    // (Midnight/LastThird tuck under Isha; Imsak stays a normal dawn row — its Isha is hours away)
                    items = shown.mapIndexed { i, mt ->
                        val parent = mt.miqat.group
                        val prev = shown.getOrNull(i - 1)?.miqat
                        val nested = parent != null && (prev == parent || prev?.group == parent)
                        AppTileItem(
                            title = mt.miqat.label(selected),
                            leading = if (nested) {
                                {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Spacer(Modifier.width(22.dp))
                                        Icon(mt.miqat.icon, null, tint = mt.miqat.color.copy(alpha = 0.55f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            } else null,
                            leadingIcon = if (nested) null else mt.miqat.icon,
                            leadingColor = if (nested) null else mt.miqat.color,
                            trailing = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        mt.at.time.format(timeFormat.pattern),
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppTheme.colors.onSurface,
                                    )
                                    if (mt.miqat == Miqat.Imsak) {
                                        Spacer(Modifier.width(8.dp))
                                        Icon(Lucide.Info, "About Imsak", tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            onClick = if (mt.miqat == Miqat.Imsak) { { showSehriInfo = true } } else null,
                        )
                    },
                )

                if (showSehriInfo) SehriInfoSheet(onDismiss = { showSehriInfo = false })
            }
        }
}

private fun formatSelected(date: LocalDate): String {
    val day = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val month = Month(date.monthNumber).name.lowercase().replaceFirstChar { it.uppercase() }
    return "$day, ${date.dayOfMonth} $month ${date.year}"
}
