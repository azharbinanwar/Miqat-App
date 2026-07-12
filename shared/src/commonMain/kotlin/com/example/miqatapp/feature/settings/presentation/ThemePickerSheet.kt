package com.example.miqatapp.feature.settings.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Monitor
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Sun
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.config.theme.ThemeChoice
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.appearance
import org.jetbrains.compose.resources.stringResource

@Composable
fun ThemePickerSheet(
    current: ThemeChoice,
    onSelect: (ThemeChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    val primary = AppTheme.colors.primary
    AppBottomSheet(onDismiss = onDismiss, title = stringResource(Res.string.appearance)) {
        AppTileGroup(
            modifier = Modifier.padding(top = 4.dp),
            items = ThemeChoice.entries.map { choice ->
                val selected = choice == current
                AppTileItem(
                    title = choice.label(),
                    leadingIcon = iconFor(choice),
                    leadingColor = primary,
                    selected = selected,
                    trailing = if (selected) {
                        { Icon(Lucide.Check, null, tint = primary) }
                    } else null,
                    onClick = { onSelect(choice) },
                )
            },
        )
    }
}

private fun iconFor(choice: ThemeChoice): ImageVector = when (choice) {
    ThemeChoice.Light -> Lucide.Sun
    ThemeChoice.Dark -> Lucide.Moon
    ThemeChoice.System -> Lucide.Monitor
}
