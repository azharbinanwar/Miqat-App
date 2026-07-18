package com.example.miqatapp.feature.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.CloudSun
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Sun
import com.example.miqatapp.core.enums.WidgetColor
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.miqat_logo
import org.jetbrains.compose.resources.painterResource

// 1:1 Compose replicas of the widgets. The box is the real cell footprint: a screen has COLUMNS cells across,
// one cell = availableWidth / COLUMNS, and rows run ROW_RATIO taller than a column is wide. Every inner value
// is the exact widget XML number × F — one knob scales all text/padding/sizes together to fit the preview cell.
private const val COLUMNS = 4
private const val ROW_RATIO = 1.18f
private const val F = 0.72f // one factor for every inner size

private fun fsp(v: Number) = (v.toFloat() * F).sp
private fun fdp(v: Number) = (v.toFloat() * F).dp

private fun cardBrush(color: WidgetColor, opacity: Float) =
    Brush.linearGradient(
        listOf(color.fill.copy(alpha = opacity), color.fillEnd.copy(alpha = opacity)),
        start = Offset(0f, 0f), end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

@Composable
private fun CellWidget(cols: Int, rows: Int, modifier: Modifier, square: Boolean = false, content: @Composable () -> Unit) {
    BoxWithConstraints(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val cell = maxWidth.value / COLUMNS
        val w = cell * cols
        val h = if (square) w else cell * rows * ROW_RATIO
        Box(Modifier.width(w.dp).height(h.dp)) { content() }
    }
}

/** Prayer Times (prayer_times_widget.xml) — 4×1. */
@Composable
fun PrayerTimesPreview(color: WidgetColor, opacity: Float, modifier: Modifier = Modifier) {
    val on = color.on
    CellWidget(cols = 4, rows = 1, modifier = modifier) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(fdp(20))).background(cardBrush(color, opacity))) {
            Icon(painterResource(Res.drawable.miqat_logo), null, tint = on.copy(alpha = 0.06f),
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = fdp(34), y = fdp(34)).size(fdp(150)))
            Row(Modifier.fillMaxSize().padding(horizontal = fdp(18), vertical = fdp(10)), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Friday · 3 Safar", color = on, fontSize = fsp(11), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("Dhuhr", color = on, fontSize = fsp(34), fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = fdp(2)))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("NEXT", color = on, fontSize = fsp(10), fontWeight = FontWeight.Bold)
                    Text("Asr", color = on, fontSize = fsp(20), fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = fdp(1)))
                    Text("4:41 PM · in 2:14", color = on, fontSize = fsp(12), maxLines = 1, modifier = Modifier.padding(top = fdp(2)))
                }
            }
        }
    }
}

/** Prayer Bar (prayer_bar_widget.xml) — 4×1. */
@Composable
fun PrayerBarPreview(color: WidgetColor, opacity: Float, modifier: Modifier = Modifier) {
    val on = color.on
    CellWidget(cols = 4, rows = 1, modifier = modifier) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(fdp(20))).background(cardBrush(color, opacity))) {
            Icon(painterResource(Res.drawable.miqat_logo), null, tint = on.copy(alpha = 0.06f),
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = fdp(28), y = fdp(28)).size(fdp(120)))
            Row(Modifier.fillMaxSize().padding(horizontal = fdp(18)), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("NOW", color = on, fontSize = fsp(10), fontWeight = FontWeight.Bold)
                    Text("Dhuhr · 12:34 PM", color = on, fontSize = fsp(20), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = fdp(2)))
                }
                Box(Modifier.padding(horizontal = fdp(14)).width(fdp(1)).height(fdp(32)).background(on.copy(alpha = 0.20f)))
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("NEXT", color = on, fontSize = fsp(10), fontWeight = FontWeight.Bold)
                    Text("Asr · 3:58 PM", color = on, fontSize = fsp(20), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = fdp(2)))
                }
            }
        }
    }
}

/** Prayer Card (prayer_card_widget.xml) — 4×2. */
@Composable
fun PrayerCardPreview(color: WidgetColor, opacity: Float, modifier: Modifier = Modifier) {
    val on = color.on
    CellWidget(cols = 4, rows = 2, modifier = modifier) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(fdp(28))).background(cardBrush(color, opacity))) {
            Icon(painterResource(Res.drawable.miqat_logo), null, tint = on.copy(alpha = 0.06f),
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = fdp(36), y = fdp(36)).size(fdp(160)))
            Column(Modifier.fillMaxSize().padding(start = fdp(20), end = fdp(20), top = fdp(18), bottom = fdp(16))) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("NOW", color = on, fontSize = fsp(11), fontWeight = FontWeight.Bold)
                        Text("Dhuhr", color = on, fontSize = fsp(32), fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = fdp(3)))
                        Text("Next · Asr · in 2:14", color = on, fontSize = fsp(14), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = fdp(5)))
                    }
                    Box(Modifier.size(fdp(50)).clip(RoundedCornerShape(fdp(15))).background(on.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(Lucide.Sun, null, tint = on, modifier = Modifier.size(fdp(26)))
                    }
                }
                Spacer(Modifier.weight(1f))
                Box(Modifier.fillMaxWidth().height(fdp(1)).background(on.copy(alpha = 0.12f)))
                Spacer(Modifier.height(fdp(12)))
                Row(Modifier.fillMaxWidth()) {
                    listOf("Fajr" to "3:34 AM", "Asr" to "5:01 PM", "Maghrib" to "7:07 PM", "Isha" to "8:42 PM").forEach { (n, t) ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(n, color = on, fontSize = fsp(14), fontWeight = FontWeight.Bold, maxLines = 1)
                            Text(t, color = on, fontSize = fsp(17), maxLines = 1, modifier = Modifier.padding(top = fdp(4)))
                        }
                    }
                }
            }
        }
    }
}

/** Prayer Minimal (minimal_widget.xml) — 2×2. */
@Composable
fun PrayerMinimalPreview(color: WidgetColor, opacity: Float, modifier: Modifier = Modifier) {
    val on = color.on
    CellWidget(cols = 2, rows = 2, modifier = modifier, square = true) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(fdp(20))).background(cardBrush(color, opacity))) {
            Icon(painterResource(Res.drawable.miqat_logo), null, tint = on.copy(alpha = 0.07f),
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = fdp(26), y = fdp(26)).size(fdp(120)))
            Column(Modifier.fillMaxSize().padding(fdp(18)), verticalArrangement = Arrangement.Center) {
                Text("NOW", color = on, fontSize = fsp(11), fontWeight = FontWeight.Bold)
                Text("Dhuhr", color = on, fontSize = fsp(33), fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = fdp(7)))
                Text("Next · Asr · 4:41", color = on, fontSize = fsp(13), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = fdp(6)))
                Text("in 2:14", color = on, fontSize = fsp(13), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = fdp(12)))
            }
        }
    }
}

/** Current Prayer (prayer_next_widget.xml) — 2×2, centred. */
@Composable
fun PrayerCurrentPreview(color: WidgetColor, opacity: Float, modifier: Modifier = Modifier) {
    val on = color.on
    CellWidget(cols = 2, rows = 2, modifier = modifier, square = true) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(fdp(20))).background(cardBrush(color, opacity))) {
            Icon(painterResource(Res.drawable.miqat_logo), null, tint = on.copy(alpha = 0.07f),
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = fdp(26), y = fdp(26)).size(fdp(120)))
            Column(Modifier.fillMaxSize().padding(fdp(14)), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("NOW", color = on, fontSize = fsp(11), fontWeight = FontWeight.Bold)
                Text("Asr", color = on, fontSize = fsp(27), fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = fdp(6)))
                Text("3:58 PM", color = on, fontSize = fsp(14), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = fdp(4)))
            }
        }
    }
}

/** Prayer Tile (prayer_tile_widget.xml) — 2×2, icon + name + time. */
@Composable
fun PrayerTilePreview(color: WidgetColor, opacity: Float, modifier: Modifier = Modifier) {
    val on = color.on
    CellWidget(cols = 2, rows = 2, modifier = modifier, square = true) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(fdp(20))).background(cardBrush(color, opacity))) {
            Icon(painterResource(Res.drawable.miqat_logo), null, tint = on.copy(alpha = 0.07f),
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = fdp(26), y = fdp(26)).size(fdp(120)))
            Column(Modifier.fillMaxSize().padding(fdp(14)), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Lucide.CloudSun, null, tint = on, modifier = Modifier.size(fdp(30)))
                Text("ASR", color = on, fontSize = fsp(11), fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = fdp(8)))
                Text("3:58", color = on, fontSize = fsp(28), fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = fdp(2)))
            }
        }
    }
}

/** Prayer Icon (prayer_icon_widget.xml) — 1×1 rounded-square app-icon tile. */
@Composable
fun PrayerIconPreview(color: WidgetColor, opacity: Float, modifier: Modifier = Modifier) {
    val on = color.on
    CellWidget(cols = 1, rows = 1, modifier = modifier, square = true) {
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(fdp(8))).background(cardBrush(color, opacity))) {
            Column(Modifier.fillMaxSize().padding(fdp(8)), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ASR", color = on, fontSize = fsp(10), fontWeight = FontWeight.Bold, maxLines = 1)
                Text("3:58", color = on, fontSize = fsp(20), fontWeight = FontWeight.Bold, maxLines = 1, modifier = Modifier.padding(top = fdp(1)))
            }
        }
    }
}
