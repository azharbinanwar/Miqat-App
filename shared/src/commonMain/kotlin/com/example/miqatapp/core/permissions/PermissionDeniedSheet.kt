package com.example.miqatapp.core.permissions

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.widgets.AppBottomSheet
import com.example.miqatapp.core.widgets.AppButton
import com.example.miqatapp.core.widgets.AppButtonVariant
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.cancel
import com.example.miqatapp.resources.open_settings
import org.jetbrains.compose.resources.stringResource

/**
 * Explains why a permission is needed, then routes to system Settings — in the app's bottom sheet so it
 * matches the rest of the UI. Reusable across permissions (Location now, Notifications later); the caller
 * supplies the title + reason for that permission.
 */
@Composable
fun PermissionDeniedSheet(
    title: String,
    message: String,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(
        onDismiss = onDismiss,
        title = title,
        footer = {
            AppButton(stringResource(Res.string.open_settings), onOpenSettings, Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            AppButton(stringResource(Res.string.cancel), onDismiss, Modifier.fillMaxWidth(), variant = AppButtonVariant.Text)
        },
    ) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurfaceVariant)
    }
}
