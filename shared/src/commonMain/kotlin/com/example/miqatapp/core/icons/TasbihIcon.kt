package com.example.miqatapp.core.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

private var cached: ImageVector? = null

/**
 * Our own tasbih: a loop of 8 beads with a hanging tassel — a miniature of the counter screen.
 * Drawn to Lucide spec (24x24 viewport, 2px round stroke) so it blends with the rest of the set.
 * Tinted like any other icon via [androidx.compose.material3.Icon]'s tint.
 */
val TasbihIcon: ImageVector
    get() {
        cached?.let { return it }
        val stroke = SolidColor(Color(0xFF000000))
        val built = ImageVector.Builder(
            name = "Tasbih",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            // one bead = tiny stroked circle (renders as a Lucide-style dot)
            fun bead(cx: Float, cy: Float) {
                path(
                    stroke = stroke,
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                ) {
                    moveTo(cx - 1f, cy)
                    arcToRelative(1f, 1f, 0f, true, false, 2f, 0f)
                    arcToRelative(1f, 1f, 0f, true, false, -2f, 0f)
                }
            }
            // the loop — ring of beads centered (12, 9.5), bottom bead is the head bead
            bead(12f, 3f)
            bead(16.6f, 4.9f)
            bead(18.5f, 9.5f)
            bead(16.6f, 14.1f)
            bead(12f, 16f)
            bead(7.4f, 14.1f)
            bead(5.5f, 9.5f)
            bead(7.4f, 4.9f)
            // the tassel — short stem off the head bead, two strands
            path(
                stroke = stroke,
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            ) {
                moveTo(12f, 18f)
                lineTo(12f, 19.5f)
                moveTo(12f, 19.5f)
                lineTo(10.3f, 21.8f)
                moveTo(12f, 19.5f)
                lineTo(13.7f, 21.8f)
            }
        }.build()
        cached = built
        return built
    }
