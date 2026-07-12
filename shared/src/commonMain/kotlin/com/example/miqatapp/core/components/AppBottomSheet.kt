package com.example.miqatapp.core.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    title: String? = null,                                 // pinned header — stays put while the body scrolls
    subtitle: String? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null, // pinned below the scrollable body (e.g. action buttons)
    fillHeight: Boolean = false,                          // true = body fills to the max height, so it stays put while a list filters
    content: @Composable ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // let the body grow up to 85% of the screen before it scrolls
    val winH = LocalWindowInfo.current.containerSize.height
    val maxSheetHeight = if (winH > 0) with(LocalDensity.current) { (winH * 0.85f).toDp() } else 520.dp
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
        scrimColor = Color.Black.copy(alpha = 0.32f), // real dim so the sheet reads as a floating panel (matches AppDrawer)
        // float on every side: keep clear of status bar (top) AND nav bar (bottom) so it never touches the top
        modifier = modifier
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 10.dp, vertical = 12.dp),
    ) {
        Column(Modifier.fillMaxWidth().then(if (fillHeight) Modifier.height(maxSheetHeight) else Modifier)) {
            // pinned header — doesn't scroll with the body
            if (title != null) {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 8.dp)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AppTheme.colors.onSurface)
                    if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = AppTheme.colors.onSurfaceVariant)
                }
            }
            // scrollable body — capped so a sticky footer always stays visible
            Column(
                Modifier.fillMaxWidth()
                    .then(if (fillHeight) Modifier.weight(1f) else Modifier.heightIn(max = maxSheetHeight))
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = if (footer == null) 16.dp else 8.dp),
                content = content,
            )
            if (footer != null) {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp), content = footer)
            }
        }
    }
}
