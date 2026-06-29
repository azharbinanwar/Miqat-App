package com.example.miqatapp.core.widgets

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme

/** A live "pulse" indicator: a solid dot with an expanding, fading ring. Reusable anywhere. */
@Composable
fun PulseDot(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.primary,
    size: Dp = 9.dp,
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        1f, 2.4f, infiniteRepeatable(tween(1100), RepeatMode.Restart), label = "scale",
    )
    val ringAlpha by transition.animateFloat(
        0.5f, 0f, infiniteRepeatable(tween(1100), RepeatMode.Restart), label = "alpha",
    )
    Box(modifier.size(size * 2.4f), contentAlignment = Alignment.Center) {
        Box(Modifier.size(size).scale(scale).alpha(ringAlpha).clip(CircleShape).background(color))
        Box(Modifier.size(size).clip(CircleShape).background(color))
    }
}
