package com.example.miqatapp.feature.home.presentation.components

import androidx.compose.foundation.background
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
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.feature.home.presentation.MosqueScene
import com.example.miqatapp.feature.home.presentation.SkyState
import org.jetbrains.compose.resources.stringResource

/**
 * Collapsing prayer header with the live moon/sun scene (position driven by [sky]).
 * Caller computes [fraction] (0 = expanded, 1 = collapsed) from scroll.
 */
@Composable
fun PrayerSceneHeader(
    prayer: Miqat,
    period: Miqat,
    sky: SkyState,
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
) {
    val headerHeight = lerp(expandedHeight, collapsedHeight, fraction)
    val headerCorner = lerp(28.dp, 0.dp, fraction)
    val expandedAlpha = (1f - fraction * 1.7f).coerceIn(0f, 1f)
    val slimAlpha = ((fraction - 0.35f) / 0.65f).coerceIn(0f, 1f)

    Box(
        modifier.fillMaxWidth().height(headerHeight)
            .clip(RoundedCornerShape(bottomStart = headerCorner, bottomEnd = headerCorner)),
    ) {
        MosqueScene(sky, modifier = Modifier.fillMaxSize())
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
                Text("NOW", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.6.sp)
                Spacer(Modifier.height(3.dp))
                Text(stringResource(period.labelRes), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Text("Next · ${prayer.name}   $nextTime · $countdown", color = Color.White.copy(alpha = 0.92f), fontSize = 13.sp)
                Spacer(Modifier.height(3.dp))
                Text(dateLabel, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
    }
}
