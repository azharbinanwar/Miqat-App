package com.example.miqatapp.feature.notifications.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.miqatapp.core.components.AppSwitch
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.MiniStepper
import com.example.miqatapp.core.constants.defaults.NotificationDefaults as N
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
fun NotifDhikr() {
    val min = stringResource(Res.string.minutes_short)
    val d by NotificationStore.settings.collectAsState()
    val dhikr = d.dhikr

    DhikrGroup(stringResource(Res.string.morning_adhkar), stringResource(Res.string.after_fajr), dhikr.morningEnabled, NotificationStore::setMorningEnabled, dhikr.afterFajr, NotificationStore::setMorningAfter, min)
    DhikrGroup(stringResource(Res.string.evening_adhkar), stringResource(Res.string.after_asr), dhikr.eveningEnabled, NotificationStore::setEveningEnabled, dhikr.afterAsr, NotificationStore::setEveningAfter, min)
}

@Composable
private fun DhikrGroup(title: String, offsetLabel: String, on: Boolean, onToggle: (Boolean) -> Unit, after: Int, onAfter: (Int) -> Unit, min: String) {
    AppTileGroup(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        items = buildList {
            add(AppTileItem(title = title, subtitle = offsetLabel, trailing = { AppSwitch(checked = on, onCheckedChange = onToggle) }))
            if (on) add(AppTileItem(title = offsetLabel, trailing = { MiniStepper(after, min, onAfter, min = N.Dhikr.offsetMin, max = N.Dhikr.offsetMax, step = N.Dhikr.step) }))
        },
    )
}
