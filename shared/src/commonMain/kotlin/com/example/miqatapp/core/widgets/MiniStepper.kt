package com.example.miqatapp.core.widgets

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Plus
import com.example.miqatapp.config.theme.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * Compact ± value stepper. Tap = ±[step] (1 by default); press-and-hold keeps stepping, accelerating the
 * longer you hold — so reaching 120 is one hold, not 120 taps. Clamps to [min]..[max].
 */
@Composable
fun MiniStepper(value: Int, suffix: String, onChange: (Int) -> Unit, min: Int, max: Int, step: Int = 1) {
    val c = AppTheme.colors
    val current by rememberUpdatedState(value)
    Row(verticalAlignment = Alignment.CenterVertically) {
        RepeatButton(Lucide.Minus, "Less", enabled = value > min) { onChange((current - step).coerceAtLeast(min)) }
        Text("$value $suffix", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.onSurface, modifier = Modifier.width(64.dp), textAlign = TextAlign.Center, maxLines = 1, softWrap = false)
        RepeatButton(Lucide.Plus, "More", enabled = value < max) { onChange((current + step).coerceAtMost(max)) }
    }
}

/** A ± button that fires once on tap and auto-repeats (accelerating) while held. */
@Composable
private fun RepeatButton(icon: ImageVector, label: String, enabled: Boolean, onStep: () -> Unit) {
    val c = AppTheme.colors
    val scope = rememberCoroutineScope()
    val step by rememberUpdatedState(onStep)
    Box(
        Modifier.size(34.dp).pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onPress = {
                    val job = scope.launch {
                        step()                  // immediate first step
                        delay(400.milliseconds)              // hold threshold before auto-repeat kicks in
                        var period = 110L
                        while (isActive) {
                            step()
                            delay(period)
                            if (period > 35) period -= 12 // accelerate
                        }
                    }
                    tryAwaitRelease()
                    job.cancel()
                },
            )
        },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, label, tint = if (enabled) c.onSurface else c.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
    }
}
