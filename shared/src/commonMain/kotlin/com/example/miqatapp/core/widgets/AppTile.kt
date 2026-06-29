package com.example.miqatapp.core.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme

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
) {
    val c = AppTheme.colors
    val bg = if (selected) c.primary.copy(alpha = 0.10f) else c.cardColor
    Column(modifier.fillMaxWidth().clip(shapeFor(position)).background(bg)) {
        TileRow(title, subtitle, leadingIcon, leadingColor, leading, trailing, badge, selected, onClick)
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
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items.forEachIndexed { i, item ->
                AppTile(
                    title = item.title,
                    subtitle = item.subtitle,
                    leadingIcon = item.leadingIcon,
                    leadingColor = item.leadingColor,
                    leading = item.leading,
                    trailing = item.trailing,
                    badge = item.badge,
                    selected = item.selected,
                    position = positionFor(i, items.size),
                    onClick = item.onClick,
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
) {
    val c = AppTheme.colors
    var row = Modifier.fillMaxWidth()
    if (onClick != null) row = row.clickable(onClick = onClick)
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
                Text("›", style = MaterialTheme.typography.titleLarge, color = if (selected) c.primary else c.onSurfaceVariant)
            }
        }
    }
}
