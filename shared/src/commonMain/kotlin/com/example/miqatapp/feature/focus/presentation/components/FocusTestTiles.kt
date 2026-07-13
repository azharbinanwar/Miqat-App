package com.example.miqatapp.feature.focus.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.BellOff
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.focus.PhoneSilencer

// Dev-only test tiles (revealed by the 7-tap gesture). Strings inline; never shown to end users.
@Composable
fun FocusTestTiles() {
    var showTest by remember { mutableStateOf(false) }
    AppTileGroup(
        modifier = Modifier.fillMaxWidth(),
        items = listOf(
            AppTileItem(
                title = "Test now (mute 5s)",
                subtitle = "Mutes for 5 seconds, then restores",
                leadingIcon = Lucide.BellOff,
                onClick = { PhoneSilencer.silenceFor(5000) },
            ),
            AppTileItem(
                title = "Background test",
                subtitle = "Pick a time, kill the app, confirm it mutes and restores on its own",
                leadingIcon = Lucide.Clock,
                onClick = { showTest = true },
            ),
        ),
    )
    Spacer(Modifier.height(12.dp))
    if (showTest) FocusTestSheet(onDismiss = { showTest = false })
}
