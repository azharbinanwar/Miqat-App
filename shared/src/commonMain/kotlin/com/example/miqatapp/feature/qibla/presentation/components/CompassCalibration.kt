package com.example.miqatapp.feature.qibla.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.compass_accuracy_low_wave_phone_in_figure_eight
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shown while the compass is uncalibrated: a phone glides along a dashed figure-8, the motion
 * the user should mimic to recalibrate the magnetometer. Pure vector — stays crisp at any size.
 */
@Composable
fun CompassCalibration(modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val t by rememberInfiniteTransition(label = "calibration").animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = 3200, easing = LinearEasing)),
        label = "t",
    )

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Canvas(Modifier.size(width = 200.dp, height = 132.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val ax = size.width * 0.32f          // horizontal reach of the 8
            val ay = size.height * 0.30f         // vertical reach

            // dashed figure-8 guide (Gerono lemniscate): x = sin s, y = 2·sin s·cos s
            val guide = Path()
            var s = 0f
            while (s <= 2f * PI.toFloat()) {
                val gx = cx + ax * sin(s)
                val gy = cy + ay * 2f * sin(s) * cos(s)
                if (s == 0f) guide.moveTo(gx, gy) else guide.lineTo(gx, gy)
                s += 0.05f
            }
            drawPath(
                guide,
                color = c.onSurfaceVariant.copy(alpha = 0.28f),
                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 9f))),
            )

            // the phone, riding the curve with a gentle wobble
            val px = cx + ax * sin(t)
            val py = cy + ay * 2f * sin(t) * cos(t)
            val w = 26.dp.toPx()
            val h = 46.dp.toPx()
            val r = 6.dp.toPx()
            rotate(degrees = 16f * sin(t), pivot = Offset(px, py)) {
                drawRoundRect(
                    color = c.primary,
                    topLeft = Offset(px - w / 2f, py - h / 2f),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(r, r),
                )
                drawRoundRect(
                    color = c.onPrimary.copy(alpha = 0.9f),
                    topLeft = Offset(px - w / 2f + 3.dp.toPx(), py - h / 2f + 5.dp.toPx()),
                    size = Size(w - 6.dp.toPx(), h - 11.dp.toPx()),
                    cornerRadius = CornerRadius(r / 2f, r / 2f),
                )
            }
        }

        Text(
            stringResource(Res.string.compass_accuracy_low_wave_phone_in_figure_eight),
            style = MaterialTheme.typography.bodyMedium,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}
