package com.example.miqatapp.core.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.sehri_ends_at
import com.example.miqatapp.resources.sehri_info_body
import com.example.miqatapp.resources.sehri_info_regions
import com.example.miqatapp.resources.sehri_info_title
import org.jetbrains.compose.resources.stringResource

/**
 * Explains Sehri / Imsak / Iftar and lets the user choose which time the Ramadan "Sehri" label follows —
 * Fajr (the ruling: eat until true dawn) or Imsak (the ~10-min-early precaution). Reads and writes
 * [SettingsStore.sehriReference], so the choice persists and updates every screen live. Reachable only
 * from Ramadan surfaces (the Home chips and the Prayer Times Imsak row), so the switch is always relevant.
 */
@Composable
fun SehriInfoSheet(onDismiss: () -> Unit) {
    val c = AppTheme.colors
    val current by SettingsStore.sehriReference.collectAsState()
    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.sehri_info_title)) {
        Text(stringResource(Res.string.sehri_info_body), fontSize = 14.sp, color = c.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Text(stringResource(Res.string.sehri_info_regions), fontSize = 13.sp, color = c.onSurfaceVariant.copy(alpha = 0.85f))
        Spacer(Modifier.height(16.dp))
        AppTileGroup(
            title = stringResource(Res.string.sehri_ends_at),
            items = listOf(Miqat.Fajr, Miqat.Imsak).map { m ->
                AppTileItem(
                    title = m.label(),
                    leadingIcon = m.icon,
                    selected = m == current,
                    trailing = { if (m == current) Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(20.dp)) },
                    onClick = { SettingsStore.setSehriReference(m) },
                )
            },
        )
    }
}
