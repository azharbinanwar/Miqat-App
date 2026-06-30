package com.example.miqatapp.feature.prayer.presentation

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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.example.miqatapp.core.enums.Prayer
import com.example.miqatapp.core.enums.color
import com.example.miqatapp.core.widgets.LocalDrawerState
import com.example.miqatapp.core.widgets.AppTileGroup
import com.example.miqatapp.core.widgets.AppTileItem
import com.example.miqatapp.core.widgets.PulseDot
import com.example.miqatapp.feature.prayer.presentation.components.MonthCalendar
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.prayer_times
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch
import com.example.miqatapp.core.datetime.currentDate
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/**
 * Calendar-first prayer times. Today is shown on Home; here you pick any date.
 * Times are placeholder until the Adhan repo lands.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen() {
    val today =
        remember { currentDate() }
    var visible by remember { mutableStateOf(today) } // any day of the visible month
    var selected by remember { mutableStateOf(today) }

    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(stringResource(Res.string.prayer_times), fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, "Menu") }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO settings */ }) { Icon(Lucide.Settings, "Settings") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = AppTheme.colors.scaffoldBackgroundColor,
                        scrolledContainerColor = Color.Unspecified,
                        navigationIconContentColor = AppTheme.colors.onSurface,
                        titleContentColor = AppTheme.colors.onSurface,
                        actionIconContentColor = AppTheme.colors.onSurface
                    ),
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
                    items = Prayer.entries.map { p ->
                        AppTileItem(
                            title = p.name,
                            selected = selected == today && p == Prayer.Dhuhr,
                            leadingIcon = p.icon,
                            leadingColor = p.color,
                            trailing = {
                                Text(
                                    mockTime(selected, p),
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.onSurface,
                                )
                            },
                            // ponytail: real "current prayer" needs Adhan + now; mock highlights Dhuhr on today
                            badge = if (selected == today && p == Prayer.Dhuhr) {
                                { PulseDot(color = AppTheme.colors.primary) }
                            } else null,
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

/** Placeholder times until PrayerRepository (Adhan) is wired. Deterministic per date. */
private fun mockTime(date: LocalDate, prayer: Prayer): String {
    val base = when (prayer) {
        Prayer.Fajr -> 5 * 60 + 12
        Prayer.Sunrise -> 6 * 60 + 34
        Prayer.Dhuhr -> 12 * 60 + 21
        Prayer.Asr -> 15 * 60 + 47
        Prayer.Maghrib -> 18 * 60 + 14
        Prayer.Isha -> 19 * 60 + 42
    }
    val m = base + (date.dayOfMonth % 7) - 3
    return "${(m / 60).toString().padStart(2, '0')}:${(m % 60).toString().padStart(2, '0')}"
}
