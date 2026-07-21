package com.example.miqatapp.feature.quran.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ArrowLeftRight
import com.composables.icons.lucide.Bookmark
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.FastForward
import com.composables.icons.lucide.FolderPlus
import com.composables.icons.lucide.Highlighter
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Image
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.Link
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Mic
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Repeat
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.StickyNote
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.SHEET_SCRIM_ALPHA
import kotlinx.coroutines.launch

private class QAction(val icon: ImageVector, val label: String)
private class QGroup(val title: String, val items: List<QAction>)

// full action set from the design; ponytail: all shown now, hide non-applicable ones as features land
private val MORE_GROUPS = listOf(
    QGroup("Audio", listOf(
        QAction(Lucide.Play, "Play recitation"), QAction(Lucide.Repeat, "Repeat / loop"),
        QAction(Lucide.FastForward, "Play from here onward"), QAction(Lucide.Mic, "Choose reciter"),
    )),
    QGroup("Personal", listOf(
        QAction(Lucide.Bookmark, "Bookmark"), QAction(Lucide.FolderPlus, "Add to collection"),
        QAction(Lucide.StickyNote, "Add note"), QAction(Lucide.Highlighter, "Highlight"),
        QAction(Lucide.Check, "Mark memorized"),
    )),
    QGroup("Sharing", listOf(
        QAction(Lucide.Share2, "Share as text"), QAction(Lucide.Image, "Share as image"), QAction(Lucide.Link, "Share link"),
    )),
    QGroup("Navigation", listOf(
        QAction(Lucide.House, "Go to surah start"), QAction(Lucide.RotateCcw, "Set as last-read"),
    )),
    QGroup("Study", listOf(
        QAction(Lucide.ArrowLeftRight, "Related ayahs"), QAction(Lucide.Info, "Context of revelation"),
    )),
)

// finger-following sheet: peek shows quick actions, drag/expand reveals grouped actions + style/script
@Composable
fun AyahActionSheet(
    label: String,
    onExpandedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    val density = LocalDensity.current
    val winPx = LocalWindowInfo.current.containerSize.height
    val peekPx = with(density) { 172.dp.toPx() }
    val expandedPx = if (winPx > 0) winPx * 0.80f else with(density) { 560.dp.toPx() }

    val scope = rememberCoroutineScope()
    val heightPx = remember { Animatable(peekPx) }
    var atExpanded by remember { mutableStateOf(false) }
    val scrimAlpha by animateFloatAsState(if (atExpanded) SHEET_SCRIM_ALPHA else 0f, label = "scrim")

    fun expand() { atExpanded = true; onExpandedChange(true); scope.launch { heightPx.animateTo(expandedPx) } }
    fun collapse() { atExpanded = false; onExpandedChange(false); scope.launch { heightPx.animateTo(peekPx) } }
    fun settle() {
        val h = heightPx.value
        when {
            h < peekPx * 0.6f -> onDismiss() // flung/dragged well below the peek → close
            h > (peekPx + expandedPx) / 2f -> expand()
            else -> collapse()
        }
    }

    Box(Modifier.fillMaxSize()) {
        // scrim scales with how far the sheet is open; only intercepts taps once it's actually visible
        if (scrimAlpha > 0.01f) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = scrimAlpha))
                    .pointerInput(Unit) { detectTapGestures { collapse() } },
            )
        }
        Surface(
            color = colors.surfaceContainerHigh,
            shape = RoundedCornerShape(28.dp), // all corners rounded, like AppBottomSheet
            tonalElevation = 3.dp,
            shadowElevation = 12.dp,
            // float on every side (matches AppBottomSheet): clear the system bars, then a margin all around
            modifier = Modifier.align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 10.dp, vertical = 12.dp)
                .fillMaxWidth()
                .height(with(density) { heightPx.value.toDp() }),
        ) {
            Column(Modifier.fillMaxSize().padding(horizontal = 14.dp)) {
                // drag handle: follows the finger 1:1, settles to peek/expanded (or dismiss) on release
                Box(
                    Modifier.align(Alignment.CenterHorizontally).padding(vertical = 8.dp)
                        .size(width = 44.dp, height = 5.dp).clip(CircleShape).background(colors.outlineVariant)
                        .pointerInput(expandedPx) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { change, dy ->
                                    change.consume()
                                    scope.launch { heightPx.snapTo((heightPx.value - dy).coerceIn(peekPx * 0.4f, expandedPx)) }
                                },
                                onDragEnd = { settle() },
                            )
                        },
                )
                Text(label, color = colors.onSurfaceVariant, fontSize = 12.sp, modifier = Modifier.padding(bottom = 6.dp))
                Row(Modifier.fillMaxWidth()) {
                    QuickAction(Lucide.Play, "Play", Modifier.weight(1f)) {}
                    QuickAction(Lucide.Bookmark, "Save", Modifier.weight(1f)) {}
                    QuickAction(Lucide.Highlighter, "Highlight", Modifier.weight(1f)) {}
                    QuickAction(Lucide.Pencil, "Note", Modifier.weight(1f)) {}
                    QuickAction(Lucide.Share2, "Share", Modifier.weight(1f)) {}
                }
                // "More" affordance at the peek: tap to open (drag still works too); hidden once expanded
                if (!atExpanded) {
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { expand() }.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Lucide.ChevronUp, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("More actions", color = colors.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.size(8.dp))
                // grouped actions fill the remaining sheet height and scroll; revealed as the sheet grows
                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    MORE_GROUPS.forEach { g ->
                        AppTileGroup(title = g.title, items = g.items.map { a -> AppTileItem(title = a.label, leadingIcon = a.icon, onClick = onDismiss) })
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    // equal width via weight from the caller; clip before clickable so the ripple is a bounded rounded button
    Column(
        modifier.clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, label, tint = colors.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(4.dp))
        Text(label, color = colors.onSurfaceVariant, fontSize = 10.sp, maxLines = 1, softWrap = false)
    }
}
