package com.example.miqatapp.core.widgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme

/**
 * One bottom sheet for the whole app — pass your content in the trailing slot.
 * Floating (side margins, all-corners rounded) to match [AppDrawer]; themed background,
 * drag handle, and nav-bar-safe content padding handled here so call sites stay tiny:
 *
 *   AppBottomSheet(onDismiss = { ... }) { /* your content */ }
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null, // pinned below the scrollable body (e.g. action buttons)
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // register as an open overlay so AppDrawer blurs the app behind this sheet
    val overlay = LocalOverlay.current
    DisposableEffect(Unit) {
        overlay.sheetCount++
        onDispose { overlay.sheetCount-- }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.surfaceContainerHigh, // distinct from scaffold + cardColor so the edge and inner tiles read in both themes
        shape = RoundedCornerShape(28.dp),
        scrimColor = AppTheme.colors.primary.copy(alpha = 0.10f), // subtle on-brand dim (+ blur) so the sheet stands out in dark & light
        // float on every side: keep clear of status bar (top) AND nav bar (bottom) so it never touches the top
        modifier = modifier
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 10.dp, vertical = 12.dp),
    ) {
        Column(Modifier.fillMaxWidth()) {
            // scrollable body — capped so a sticky footer always stays visible
            Column(
                Modifier.fillMaxWidth().heightIn(max = 520.dp).verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = if (footer == null) 16.dp else 8.dp),
                content = content,
            )
            if (footer != null) {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp), content = footer)
            }
        }
    }
}
