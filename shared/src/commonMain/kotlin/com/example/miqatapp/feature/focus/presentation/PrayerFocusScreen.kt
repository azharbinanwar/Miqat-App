package com.example.miqatapp.feature.focus.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.feature.focus.presentation.components.FocusNeedsAttention
import com.example.miqatapp.feature.focus.presentation.components.FocusPrayerList
import com.example.miqatapp.feature.focus.presentation.components.FocusTestTiles
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.prayer_focus
import com.example.miqatapp.resources.silence_phone_around_each_prayer_set_separately
import org.jetbrains.compose.resources.stringResource

// Prayer Focus (Android only): mute the phone around each prayer. Just layout; the pieces own their state.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerFocusScreen(onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    var taps by remember { mutableStateOf(0) } // 7 taps on the blurb reveals the test tiles; resets on re-entry
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.prayer_focus)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text(
                stringResource(Res.string.silence_phone_around_each_prayer_set_separately),
                fontSize = 13.sp, color = c.onSurfaceVariant,
                // hidden dev gesture: no ripple, so it doesn't look tappable
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { taps++ },
            )
            Spacer(Modifier.height(12.dp))
            FocusNeedsAttention()
            if (taps >= 7) FocusTestTiles()
            FocusPrayerList()
            Spacer(Modifier.height(8.dp))
        }
    }
}
