package com.example.miqatapp.feature.home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Menu
import com.example.miqatapp.core.enums.Prayer
import com.example.miqatapp.feature.home.presentation.MosqueScene
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.tap_header_to_preview_each_prayer
import org.jetbrains.compose.resources.stringResource

/**
 * Collapsing prayer header with the animated moon/sun scene. Tap to switch prayer.
 * Caller computes [fraction] (0 = expanded, 1 = collapsed) from scroll.
 */
@Composable
fun PrayerSceneHeader(
    prayer: Prayer,
    fraction: Float,
    locationName: String,
    dateLabel: String,
    nextTime: String,
    countdown: String,
    modifier: Modifier = Modifier,
    expandedHeight: Dp = 380.dp,
    collapsedHeight: Dp = 116.dp,
    onMenuClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onTap: () -> Unit = {},
) {
    val headerHeight = lerp(expandedHeight, collapsedHeight, fraction)
    val headerCorner = lerp(28.dp, 0.dp, fraction)
    val expandedAlpha = (1f - fraction * 1.7f).coerceIn(0f, 1f)
    val slimAlpha = ((fraction - 0.35f) / 0.65f).coerceIn(0f, 1f)

    Box(
        modifier.fillMaxWidth().height(headerHeight)
            .clip(RoundedCornerShape(bottomStart = headerCorner, bottomEnd = headerCorner))
            .clickable { onTap() },
    ) {
        MosqueScene(prayer = prayer, modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f)))))

        Row(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMenuClick) { Icon(Lucide.Menu, "Menu", tint = Color.White) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(1f - slimAlpha)) {
                    Icon(Lucide.MapPin, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(locationName, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.alpha(slimAlpha)) {
                    Icon(prayer.icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("${prayer.name} · $nextTime", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
            IconButton(onClick = onNotificationsClick) { Icon(Lucide.Bell, "Notifications", tint = Color.White) }
        }

        if (expandedAlpha > 0f) {
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp).alpha(expandedAlpha)) {
                Text(dateLabel, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
                Spacer(Modifier.height(4.dp))
                Text("Next · ${prayer.name}", color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                Text("$nextTime  ·  $countdown", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                stringResource(Res.string.tap_header_to_preview_each_prayer),
                color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp).alpha(expandedAlpha),
            )
        }
    }
}