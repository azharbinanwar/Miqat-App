package com.example.miqatapp.core.widgets

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.example.miqatapp.core.locale.tr
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.math.roundToInt
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme
import kotlinx.coroutines.launch

/** Where a tile sits in a group — drives corner rounding (first/last differ). */
enum class TilePosition { Single, First, Middle, Last }

/** Data for one tile in an [AppTileGroup]. */
class AppTileItem(
    val title: String,
    val subtitle: String? = null,
    val leadingIcon: ImageVector? = null,
    val leadingColor: Color? = null,
    val leading: (@Composable () -> Unit)? = null,
    val trailing: (@Composable () -> Unit)? = null,
    val badge: (@Composable () -> Unit)? = null,
    val selected: Boolean = false,
    val onClick: (() -> Unit)? = null,
    val onLongClick: (() -> Unit)? = null,
)

/**
 * Borderless settings-style tile — theme-filled background (no border), grouped corners.
 * leading / title+badge / subtitle / trailing-or-chevron, selection tint.
 */
@Composable
fun AppTile(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingColor: Color? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    position: TilePosition = TilePosition.Single,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val c = AppTheme.colors
    val bg = if (selected) c.primary.copy(alpha = 0.10f) else c.cardColor
    Column(modifier.fillMaxWidth().clip(shapeFor(position)).background(bg)) {
        TileRow(title, subtitle, leadingIcon, leadingColor, leading, trailing, badge, selected, onClick, onLongClick)
    }
}

/**
 * Borderless grouped tiles: first tile top-rounded, last bottom-rounded, middle square,
 * with small gaps. Theme-filled (no border), optional section title, selection tint.
 */
@Composable
fun AppTileGroup(
    items: List<AppTileItem>,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    GroupShell(modifier, title) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEachIndexed { i, item -> Tile(item, positionFor(i, items.size)) }
        }
    }
}

/**
 * Same look as [AppTileGroup], but long-press a tile to lift it and drag up/down to reorder.
 * [onReorder] (from, to) commits on release. Opt-in variant so plain groups stay untouched.
 */
@Composable
fun AppTileGroupReorderable(
    items: List<AppTileItem>,
    onReorder: (from: Int, to: Int) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
) {
    GroupShell(modifier, title) { ReorderableTiles(items, onReorder) }
}

/** Shared chrome (outer padding + optional section title) for both group variants. */
@Composable
private fun GroupShell(modifier: Modifier, title: String?, content: @Composable () -> Unit) {
    val c = AppTheme.colors
    Column(modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        if (title != null) {
            Text(
                text = title,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
                style = MaterialTheme.typography.titleSmall,
                color = c.primary,
                fontWeight = FontWeight.Bold,
            )
        }
        content()
    }
}

@Composable
private fun Tile(item: AppTileItem, position: TilePosition) {
    AppTile(
        title = item.title,
        subtitle = item.subtitle,
        leadingIcon = item.leadingIcon,
        leadingColor = item.leadingColor,
        leading = item.leading,
        trailing = item.trailing,
        badge = item.badge,
        selected = item.selected,
        position = position,
        onClick = item.onClick,
        onLongClick = item.onLongClick,
    )
}

/** Long-press a tile to lift it, then drag up/down to reorder; onReorder(from, to) commits on release. */
@Composable
private fun ReorderableTiles(items: List<AppTileItem>, onReorder: (Int, Int) -> Unit) {
    var dragIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val heights = remember { mutableStateMapOf<Int, Int>() }
    val spacingPx = with(LocalDensity.current) { 4.dp.toPx() }
    val scope = rememberCoroutineScope()
    val di = dragIndex
    val unit = (((if (di != null) heights[di] else null) ?: 1).toFloat().coerceAtLeast(1f)) + spacingPx
    val target = if (di != null) (di + (dragOffset / unit).roundToInt()).coerceIn(0, items.lastIndex) else -1
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items.forEachIndexed { i, item ->
            val lifted = di == i
            // non-dragged tiles between the lift origin and the current target slide to open the gap
            val gapShift = when {
                di == null || lifted -> 0f
                di < target && i in (di + 1)..target -> -unit
                di > target && i in target until di -> unit
                else -> 0f
            }
            // animate the gap while dragging; snap closed on commit so neighbours don't re-slide
            val animatedShift by animateFloatAsState(gapShift, animationSpec = if (di == null) snap() else spring(), label = "reorderShift")
            Box(
                Modifier
                    .onSizeChanged { heights[i] = it.height }
                    .zIndex(if (lifted) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (lifted) dragOffset else animatedShift
                        if (lifted) { scaleX = 1.02f; scaleY = 1.02f }
                    }
                    .pointerInput(items.size, i) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { dragIndex = i; dragOffset = 0f },
                            onDrag = { change, amount -> change.consume(); dragOffset += amount.y },
                            onDragEnd = {
                                val h = (heights[i] ?: 1).toFloat().coerceAtLeast(1f) + spacingPx
                                val to = (i + (dragOffset / h).roundToInt()).coerceIn(0, items.lastIndex)
                                scope.launch {
                                    animate(dragOffset, (to - i) * h, animationSpec = tween(180)) { v, _ -> dragOffset = v } // settle into the gap
                                    if (to != i) onReorder(i, to)
                                    dragIndex = null; dragOffset = 0f
                                }
                            },
                            onDragCancel = {
                                scope.launch {
                                    animate(dragOffset, 0f, animationSpec = tween(150)) { v, _ -> dragOffset = v }
                                    dragIndex = null; dragOffset = 0f
                                }
                            },
                        )
                    },
            ) {
                AppTile(
                    title = item.title,
                    subtitle = item.subtitle,
                    leading = { Icon(Lucide.GripVertical, "Drag to reorder", tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
                    trailing = item.trailing,
                    badge = item.badge,
                    selected = item.selected,
                    position = positionFor(i, items.size),
                )
            }
        }
    }
}

private fun positionFor(index: Int, size: Int): TilePosition = when {
    size == 1 -> TilePosition.Single
    index == 0 -> TilePosition.First
    index == size - 1 -> TilePosition.Last
    else -> TilePosition.Middle
}

private fun shapeFor(pos: TilePosition): Shape {
    val r = 16.dp
    val m = 4.dp
    return when (pos) {
        TilePosition.Single -> RoundedCornerShape(r)
        TilePosition.First -> RoundedCornerShape(topStart = r, topEnd = r, bottomStart = m, bottomEnd = m)
        TilePosition.Middle -> RoundedCornerShape(m)
        TilePosition.Last -> RoundedCornerShape(topStart = m, topEnd = m, bottomStart = r, bottomEnd = r)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TileRow(
    title: String,
    subtitle: String?,
    leadingIcon: ImageVector?,
    leadingColor: Color?,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    badge: (@Composable () -> Unit)?,
    selected: Boolean,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    val c = AppTheme.colors
    var row = Modifier.fillMaxWidth()
    if (onClick != null || onLongClick != null) row = row.combinedClickable(onClick = { onClick?.invoke() }, onLongClick = onLongClick)
    Row(
        modifier = row.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            val col = leadingColor ?: c.primary
            Box(Modifier.size(38.dp).clip(CircleShape).background(col.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(leadingIcon, null, tint = col, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(16.dp))
        } else if (leading != null) {
            leading()
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f, fill = false),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) c.primary else c.onSurface,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                )
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    badge()
                }
            }
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = c.onSurfaceVariant)
            }
        }
        when {
            trailing != null -> { Spacer(Modifier.width(8.dp)); trailing() }
            onClick != null -> {
                Spacer(Modifier.width(8.dp))
                // a vector icon (not a text glyph) so RTL doesn't double-mirror it; tr picks the forward direction
                Icon(tr(Lucide.ChevronRight, Lucide.ChevronLeft), null, tint = if (selected) c.primary else c.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}
