package com.example.miqatapp.feature.notifications.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.example.miqatapp.core.components.AppSwitch
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.MiniStepper
import com.example.miqatapp.core.constants.defaults.NotificationDefaults as N
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import com.example.miqatapp.feature.notifications.presentation.shift
import com.example.miqatapp.feature.notifications.presentation.timeOf
import com.example.miqatapp.feature.notifications.presentation.timeSubtitle
import com.example.miqatapp.feature.notifications.store.NotificationStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.after_asr
import com.example.miqatapp.resources.after_fajr
import com.example.miqatapp.resources.evening_adhkar
import com.example.miqatapp.resources.minutes_short
import com.example.miqatapp.resources.morning_adhkar
import org.jetbrains.compose.resources.stringResource

// Dhikr as two self-contained groups: morning after Fajr, evening after Asr. Each expands to its offset.
@Composable
fun NotificationDhikr() {
    val min = stringResource(Res.string.minutes_short)
    val d by NotificationStore.settings.collectAsState()
    val today by MiqatTimesStore.today.collectAsState()
    val pat = SettingsStore.timeFormat.collectAsState().value.pattern
    val dhikr = d.dhikr

    val morningFire = shift(timeOf(today, Miqat.Fajr), dhikr.afterFajr)
    val eveningFire = shift(timeOf(today, Miqat.Asr), dhikr.afterAsr)
    DhikrGroup(stringResource(Res.string.morning_adhkar), stringResource(Res.string.after_fajr), morningFire, Lucide.Sunrise, dhikr.morningEnabled, NotificationStore::setMorningEnabled, dhikr.afterFajr, NotificationStore::setMorningAfter, min, pat)
    DhikrGroup(stringResource(Res.string.evening_adhkar), stringResource(Res.string.after_asr), eveningFire, Lucide.Sunset, dhikr.eveningEnabled, NotificationStore::setEveningEnabled, dhikr.afterAsr, NotificationStore::setEveningAfter, min, pat)
}

@Composable
private fun DhikrGroup(title: String, offsetLabel: String, fire: kotlinx.datetime.LocalTime?, icon: ImageVector, on: Boolean, onToggle: (Boolean) -> Unit, after: Int, onAfter: (Int) -> Unit, min: String, pat: String) {
    AppTileGroup(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        items = buildList {
            add(AppTileItem(title = title, subtitle = timeSubtitle(offsetLabel, fire, on, pat), leadingIcon = icon, trailing = { AppSwitch(checked = on, onCheckedChange = onToggle) }))
            if (on) add(AppTileItem(title = offsetLabel, trailing = { MiniStepper(after, min, onAfter, min = N.Dhikr.offsetMin, max = N.Dhikr.offsetMax, step = N.Dhikr.step) }))
        },
    )
}
