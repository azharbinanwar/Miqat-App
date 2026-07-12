package com.example.miqatapp.feature.miqat.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Settings
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.enums.color
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.feature.miqat.store.MiqatCalculationStore
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import com.example.miqatapp.feature.miqat.presentation.components.MonthCalendar
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.prayer_times
import androidx.compose.ui.graphics.Color
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

    val place by LocationStore.activePlace.collectAsState()
    val calc by MiqatCalculationStore.calculation.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val times = remember(selected, place, calc) { MiqatTimesStore.timesFor(selected) }

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
                    items = times.map { mt ->
                        AppTileItem(
                            title = stringResource(mt.miqat.labelRes),
                            leadingIcon = mt.miqat.icon,
                            leadingColor = mt.miqat.color,
                            trailing = {
                                Text(
                                    mt.at.time.format(timeFormat.pattern),
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.onSurface,
                                )
                            },
                        )
                    },
                )
            }
        }
}

private fun formatSelected(date: LocalDate): String {
    val day = date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
    val month = Month(date.monthNumber).name.lowercase().replaceFirstChar { it.uppercase() }
    return "$day, ${date.dayOfMonth} $month ${date.year}"
}
