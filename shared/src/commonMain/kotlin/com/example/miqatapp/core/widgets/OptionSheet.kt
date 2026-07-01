package com.example.miqatapp.core.widgets

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.prefs.LabeledOption
import org.jetbrains.compose.resources.stringResource

/**
 * Single-choice picker in a bottom sheet, built on [AppTileGroup]. Each option is a `(value, label)` pair —
 * the **value** is stored/compared, the **label** is shown — so the check stays correct across a locale
 * switch. Selected option shows a check (no chevron, since a trailing is provided).
 */
@Composable
fun OptionSheet(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    AppBottomSheet(onDismiss = onDismiss) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = c.onSurface)
        Spacer(Modifier.height(12.dp))
        AppTileGroup(
            items = options.map { (value, label) ->
                AppTileItem(
                    title = label,
                    selected = value == selected,
                    trailing = { if (value == selected) Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(20.dp)) },
                    onClick = { onPick(value) },
                )
            },
        )
    }
}

/**
 * Picker for a fixed enum option-set implementing [LabeledOption]. [selected] and [onPick] are the enum
 * itself — no id lookups, and the label comes straight off the entry's `labelRes`.
 */
@Composable
fun <T : LabeledOption> OptionSheet(
    title: String,
    options: List<T>,
    selected: T,
    onPick: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    AppBottomSheet(onDismiss = onDismiss) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = c.onSurface)
        Spacer(Modifier.height(12.dp))
        val items = ArrayList<AppTileItem>(options.size)
        for (option in options) {
            val isSelected = option == selected
            items.add(
                AppTileItem(
                    title = option.label(),
                    selected = isSelected,
                    trailing = { if (isSelected) Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(20.dp)) },
                    onClick = { onPick(option) },
                ),
            )
        }
        AppTileGroup(items = items)
    }
}
