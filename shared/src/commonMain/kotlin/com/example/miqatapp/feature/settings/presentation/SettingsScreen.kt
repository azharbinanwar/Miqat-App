package com.example.miqatapp.feature.settings.presentation

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.Calendar
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Globe
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Palette
import com.example.miqatapp.core.platform.canControlDnd
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.config.theme.ThemeChoice
import com.example.miqatapp.core.datetime.HijriMonth
import com.example.miqatapp.core.datetime.hijriToday
import com.example.miqatapp.core.locale.Language
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.core.prefs.TimeFormat
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.MiniStepper
import com.example.miqatapp.core.components.OptionSheet
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.feature.miqat.store.MiqatCalculationStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.about
import com.example.miqatapp.resources.all_alerts_on
import com.example.miqatapp.resources.app_name
import com.example.miqatapp.resources.appearance
import com.example.miqatapp.resources.auto_silence_around_prayer
import com.example.miqatapp.resources.days
import com.example.miqatapp.resources.general
import com.example.miqatapp.resources.hijri_calendar
import com.example.miqatapp.resources.hijri_era
import com.example.miqatapp.resources.language
import com.example.miqatapp.resources.location
import com.example.miqatapp.resources.menu
import com.example.miqatapp.resources.notifications
import com.example.miqatapp.resources.prayer_and_alerts
import com.example.miqatapp.resources.prayer_calculation
import com.example.miqatapp.resources.prayer_focus
import com.example.miqatapp.resources.settings
import com.example.miqatapp.resources.time_format
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Settings hub — a tiled menu, not a dumping ground. Basic prefs (appearance / time / language) are edited
 * inline via a small picker sheet; the heavier modules (Location, Prayer calculation, Notifications) are
 * their own screens, opened from a row. ponytail: values are mock until prefs persistence lands.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLocation: () -> Unit = {},
    onPrayerCalc: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onPrayerFocus: () -> Unit = {},
) {
    val c = AppTheme.colors
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    // basic prefs — observe the SettingsStore (resolves PrefsService ?: SettingsDefaults)
    val theme by SettingsStore.theme.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val language by SettingsStore.language.collectAsState()
    var showTheme by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    // Hijri ± day offset (moon-sighting adjustment) — the calendar page is hidden, so it's tuned here
    val hijriOffset by SettingsStore.hijriOffset.collectAsState()
    val hijri = hijriToday(hijriOffset)

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.settings), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, stringResource(Res.string.menu)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
        ) {
            AppTileGroup(
                title = stringResource(Res.string.general),
                items = listOf(
                    AppTileItem(leadingIcon = Lucide.Palette, title = stringResource(Res.string.appearance), subtitle = theme.label(), onClick = { showTheme = true }),
                    AppTileItem(leadingIcon = Lucide.Clock, title = stringResource(Res.string.time_format), subtitle = timeFormat.label(), onClick = { showTime = true }),
                    AppTileItem(leadingIcon = Lucide.Globe, title = stringResource(Res.string.language), subtitle = language.label(), onClick = { showLanguage = true }),
                    AppTileItem(
                        leadingIcon = Lucide.Calendar,
                        title = stringResource(Res.string.hijri_calendar),
                        subtitle = "${hijri.day} ${HijriMonth.of(hijri.month).label()} ${hijri.year} ${stringResource(Res.string.hijri_era)}",
                        trailing = { MiniStepper(hijriOffset, stringResource(Res.string.days), { SettingsStore.setHijriOffset(it) }, min = -2, max = 2) },
                    ),
                ),
            )
            val activeCity by LocationStore.activePlace.collectAsState()
            val asrMadhab by MiqatCalculationStore.madhab.collectAsState()
            val calcMethod by MiqatCalculationStore.method.collectAsState()
            val highLat by MiqatCalculationStore.highLatRule.collectAsState()
            AppTileGroup(
                title = stringResource(Res.string.prayer_and_alerts),
                items = buildList {
                    add(AppTileItem(leadingIcon = Lucide.MapPin, title = stringResource(Res.string.location), subtitle = activeCity.name, onClick = onLocation))
                    // madhab · method · high-lat — one line, ellipsized by the tile if long
                    add(AppTileItem(leadingIcon = Lucide.Compass, title = stringResource(Res.string.prayer_calculation), subtitle = "${asrMadhab.label} · ${calcMethod.label} · ${highLat.label}", onClick = onPrayerCalc))
                    add(AppTileItem(leadingIcon = Lucide.Bell, title = stringResource(Res.string.notifications), subtitle = stringResource(Res.string.all_alerts_on), onClick = onNotifications))
                    if (canControlDnd) add(AppTileItem(leadingIcon = Lucide.BellOff, title = stringResource(Res.string.prayer_focus), subtitle = stringResource(Res.string.auto_silence_around_prayer), onClick = onPrayerFocus))
                },
            )
            AppTileGroup(
                title = stringResource(Res.string.about),
                items = listOf(
                    AppTileItem(title = stringResource(Res.string.app_name), subtitle = "Version 1.0.0", leadingIcon = Lucide.Info),
                ),
            )
        }
    }

    if (showTheme) OptionSheet(stringResource(Res.string.appearance), ThemeChoice.entries, theme, { SettingsStore.setTheme(it); showTheme = false }) { showTheme = false }
    if (showTime) OptionSheet(stringResource(Res.string.time_format), TimeFormat.entries, timeFormat, { SettingsStore.setTimeFormat(it); showTime = false }) { showTime = false }
    if (showLanguage) OptionSheet(
        stringResource(Res.string.language),
        Language.entries, language, { SettingsStore.setLanguage(it); showLanguage = false },
    ) { showLanguage = false }
}
