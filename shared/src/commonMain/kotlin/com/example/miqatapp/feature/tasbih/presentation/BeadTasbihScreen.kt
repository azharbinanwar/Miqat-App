package com.example.miqatapp.feature.tasbih.presentation

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.RotateCcw
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.widgets.AppBottomSheet
import com.example.miqatapp.core.widgets.AppButton
import com.example.miqatapp.core.widgets.LocalDrawerState
import com.example.miqatapp.core.widgets.LocalOverlay
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.rounds
import com.example.miqatapp.resources.tasbih
import com.example.miqatapp.resources.total
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sin

/** Look & feel of the misbaha. Pass a custom one to restyle (wood / pearl / onyx / teal…). */
data class TasbihStyle(
    val beadDiameter: Dp = 44.dp,
    val beadLight: Color = Color(0xFFE7C896), // top-left highlight of the 3D bead
    val beadMid: Color = Color(0xFFA9783F),
    val beadDark: Color = Color(0xFF5C3A1E), // outer shadow
    val cord: Color = Color(0xFF7A5532),
    val gapBeads: Int = 4, // beads hidden in the "fingers" gap
)

// Ring placement on screen (from the Figma frame) — fixed, not user-facing.
private const val RADIUS_FACTOR = 0.6279f
private const val CENTER_X_FACTOR = 1.08f
private const val CENTER_Y_FACTOR = 1.014f
private const val RING_SLIDE_MS = 200 // settle animation after a swipe
private const val DRAG_DISTANCE = 3f // higher = a longer finger slide needed to move one bead

/** One completed dhikr, kept in History. ponytail: in-memory only — persist (DataStore/Room) later. */
private data class TasbihSession(val title: String, val target: Int, val date: LocalDate)

/**
 * Misbaha as a big bead ring — only the lower-left arc shows. Dragging rotates the ring one bead
 * (reversible) while one bead crosses the fixed gap; releasing past halfway counts +1 and settles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeadTasbihScreen(style: TasbihStyle? = null) {
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current

    // No style passed → user picks a material from the bottom strip (first = theme primary).
    val primary = AppTheme.colors.primary
    val materials = remember(primary) { beadMaterials(primary) }
    var selectedMaterial by remember { mutableIntStateOf(0) }
    val cfg = style ?: materials[selectedMaterial].second
    val beadR = with(density) { cfg.beadDiameter.toPx() / 2f }
    val gapHalf = cfg.gapBeads / 2f

    // the set handed off from the hub; fall back to a single default if opened directly
    val queue = remember { TasbihRun.queue.ifEmpty { listOf(Zikr("subhanallah", "SubhanAllah", "سُبْحَانَ ٱللَّٰه", 33, ZikrCategory.Tasbihat) to 33) } }

    // counter state
    var index by remember { mutableIntStateOf(0) }   // current item in the set
    var count by remember { mutableIntStateOf(0) }   // count toward the current target
    var round by remember { mutableIntStateOf(0) }   // items completed this session
    var total by remember { mutableIntStateOf(0) }   // beads counted this session
    var paused by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }   // whole set finished
    var showHistory by remember { mutableStateOf(false) }
    val history = remember { mutableListOf<TasbihSession>().toMutableStateList() }
    val curZikr = queue[index].first
    val curTarget = queue[index].second

    // ring drag state
    var angle by remember { mutableFloatStateOf(0f) }  // committed ring rotation (radians)
    var pluck by remember { mutableFloatStateOf(0f) }  // 0..1 progress of the current swipe
    var crossing by remember { mutableIntStateOf(-1) } // index of the bead crossing the gap (-1 = none)
    var crossTravel by remember { mutableIntStateOf(1) }

    fun tick() { // one bead counted
        total++
        val target = queue[index].second
        if (target > 0 && count + 1 >= target) {              // target reached (0 = unlimited → never auto-completes)
            round++
            history.add(0, TasbihSession(queue[index].first.title, target, currentDate()))
            haptics.performHapticFeedback(HapticFeedbackType.LongPress) // heavier on a completed dhikr
            if (index < queue.lastIndex) { index++; count = 0 } // auto-advance to the next dhikr in the set
            else { count = target; done = true }                // whole set finished → success
        } else {
            count++
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Don't let the drawer's edge-swipe-to-open fight the bead drag while on this screen.
    val overlay = LocalOverlay.current
    DisposableEffect(Unit) {
        overlay.drawerGesturesEnabled = false
        onDispose { overlay.drawerGesturesEnabled = true }
    }

    fun radius(w: Float, h: Float) = h * RADIUS_FACTOR
    fun center(w: Float, h: Float) = Offset(w * CENTER_X_FACTOR, h * CENTER_Y_FACTOR)
    fun beadCount(w: Float, h: Float) = (PI * radius(w, h) / beadR).roundToInt().coerceAtLeast(1)
    fun beadStep(w: Float, h: Float) = (2.0 * PI / beadCount(w, h)).toFloat()
    fun aGapOf(w: Float, h: Float): Float { // direction from the centre to the middle of the visible arc (gap centre)
        val cen = center(w, h); val r = radius(w, h)
        val twoPiF = (2.0 * PI).toFloat()
        var sx = 0f; var sy = 0f; var k = 0
        while (k < 240) {
            val sa = k * (twoPiF / 240f)
            val sp = Offset(cen.x + r * cos(sa), cen.y + r * sin(sa))
            if (sp.x in 0f..w && sp.y in 0f..h) { sx += cos(sa); sy += sin(sa) }
            k++
        }
        return atan2(sy, sx)
    }

    Scaffold(
        containerColor = AppTheme.colors.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.tasbih), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, "Menu") }
                },
                actions = {
                    IconButton(onClick = { paused = !paused }) {
                        Icon(if (paused) Lucide.Play else Lucide.Pause, if (paused) "Resume" else "Pause")
                    }
                    IconButton(onClick = { showHistory = true }) { Icon(Lucide.History, "History") }
                    IconButton(onClick = { count = 0; round = 0; index = 0; done = false }) { Icon(Lucide.RotateCcw, "Reset") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.scaffoldBackgroundColor,
                    titleContentColor = AppTheme.colors.onSurface,
                    navigationIconContentColor = AppTheme.colors.onSurface,
                    actionIconContentColor = AppTheme.colors.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                DhikrHeader(
                    arabic = curZikr.arabic,
                    translit = curZikr.title,
                    target = curTarget,
                    count = count,
                    paused = paused,
                    progress = if (queue.size > 1) "${index + 1} / ${queue.size}" else null,
                )
                Spacer(Modifier.height(16.dp))
                StatsRow(round, total)
            }

            Canvas(
                Modifier.fillMaxSize().pointerInput(Unit) {
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val r = radius(w, h)
                    val step = beadStep(w, h)
                    val n = beadCount(w, h)
                    detectVerticalDragGestures(
                        onDragStart = {
                            val gapCenterF = (aGapOf(w, h) - angle) / step
                            val bottom = floor(gapCenterF - gapHalf).toInt()
                            val top = ceil(gapCenterF + gapHalf).toInt()
                            crossing = ((bottom % n) + n) % n
                            crossTravel = (top - bottom).coerceAtLeast(1)
                            pluck = 0f
                        },
                        onDragEnd = {
                            if (pluck > 0.5f && !paused && !done) {
                                tick()
                                scope.launch {
                                    animate(pluck, 1f, animationSpec = tween(RING_SLIDE_MS)) { v, _ -> pluck = v }
                                    angle += step // commit one-bead advance (ring angle stays continuous → no jump)
                                    crossing = -1
                                    pluck = 0f
                                }
                            } else {
                                scope.launch {
                                    animate(pluck, 0f, animationSpec = tween(250)) { v, _ -> pluck = v } // reverse (didn't pass / paused)
                                    crossing = -1
                                }
                            }
                        },
                    ) { _, dy ->
                        val crossPx = step * r * DRAG_DISTANCE
                        pluck = (pluck - dy / crossPx).coerceIn(0f, 1f)
                    }
                },
            ) {
                val w = size.width
                val h = size.height
                val r = radius(w, h)
                val cen = center(w, h)
                drawCircle(cfg.cord.copy(alpha = 0.5f), radius = r, center = cen, style = Stroke(width = 3.dp.toPx()))

                val n = beadCount(w, h)
                val step = beadStep(w, h)
                val aGap = aGapOf(w, h)
                val ringAngle = angle + pluck * step // whole ring rotates one bead with the finger
                val gapCenterF = (aGap - ringAngle) / step
                val emerging = if (crossing >= 0) (((crossing + crossTravel - 1) % n) + n) % n else -1
                for (i in 0 until n) {
                    if (i == crossing || i == emerging) continue
                    val a = i * step + ringAngle
                    val p = Offset(cen.x + r * cos(a), cen.y + r * sin(a))
                    if (p.x < -beadR || p.x > w + beadR || p.y < -beadR || p.y > h + beadR) continue
                    var rel = i - gapCenterF
                    rel -= (rel / n).roundToInt() * n
                    if (abs(rel) < gapHalf) continue
                    bead(p, beadR, cfg)
                }
                if (crossing >= 0) {
                    val start = crossing * step + angle
                    val end = (crossing + crossTravel) * step + angle
                    val ca = start + pluck * (end - start)
                    bead(Offset(cen.x + r * cos(ca), cen.y + r * sin(ca)), beadR, cfg)
                }
            }

            if (style == null) {
                MaterialPicker(
                    materials = materials,
                    selected = selectedMaterial,
                    onSelect = { selectedMaterial = it },
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

            if (done) {
                SuccessOverlay(total = total) { done = false; index = 0; count = 0; round = 0 }
            }
        }
    }

    if (showHistory) {
        HistorySheet(history, onClear = { history.clear() }, onDismiss = { showHistory = false })
    }
}

@Composable
private fun DhikrHeader(arabic: String, translit: String, target: Int, count: Int, paused: Boolean, progress: String?) {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (progress != null) {
            Text(progress, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = c.primary) // set progress 1/N
            Spacer(Modifier.height(4.dp))
        }
        Text(arabic, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
        Spacer(Modifier.height(2.dp))
        Text("$translit  ·  ${if (target <= 0) "∞" else target}", fontSize = 14.sp, color = c.primary)
        Spacer(Modifier.height(10.dp))
        Text("$count", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = if (paused) c.onSurfaceVariant else c.onSurface)
        Text(if (paused) "Paused" else if (target <= 0) "∞" else "/ $target", fontSize = 14.sp, color = c.onSurfaceVariant)
    }
}

@Composable
private fun StatsRow(round: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(40.dp)) {
        Stat(round.toString(), stringResource(Res.string.rounds))
        Stat(total.toString(), stringResource(Res.string.total))
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppTheme.colors.onSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = AppTheme.colors.onSurfaceVariant)
    }
}

/** Full-screen success state shown when the whole set is finished. */
@Composable
private fun SuccessOverlay(total: Int, onDone: () -> Unit) {
    val c = AppTheme.colors
    Box(Modifier.fillMaxSize().background(c.scaffoldBackgroundColor.copy(alpha = 0.94f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("✓", fontSize = 72.sp, fontWeight = FontWeight.Bold, color = c.primary)
            Spacer(Modifier.height(8.dp))
            Text("Set complete", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = c.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("$total dhikr · MashaAllah", fontSize = 14.sp, color = c.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            AppButton("Done", onClick = onDone)
        }
    }
}

@Composable
private fun HistorySheet(history: List<TasbihSession>, onClear: () -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(onDismiss = onDismiss) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = c.onSurface)
            Spacer(Modifier.weight(1f))
            if (history.isNotEmpty()) TextButton(onClick = onClear) { Text("Clear") }
        }
        if (history.isEmpty()) {
            Text("No completed rounds yet.", color = c.onSurfaceVariant, modifier = Modifier.padding(vertical = 24.dp))
        } else {
            Column(Modifier.heightIn(max = 380.dp).verticalScroll(rememberScrollState())) {
                history.forEachIndexed { i, s ->
                    if (i > 0) HorizontalDivider(color = c.onSurfaceVariant.copy(alpha = 0.15f))
                    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(s.title, fontWeight = FontWeight.SemiBold, color = c.onSurface)
                            Text("${s.date.day}/${s.date.month.ordinal + 1}/${s.date.year}", fontSize = 12.sp, color = c.onSurfaceVariant)
                        }
                        Text("× ${s.target}", fontWeight = FontWeight.Bold, color = c.primary)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

/** A single 3D bead: radial gradient body + a small specular highlight. */
private fun DrawScope.bead(center: Offset, r: Float, style: TasbihStyle) {
    val light = Offset(center.x - r * 0.35f, center.y - r * 0.35f)
    drawCircle(
        brush = Brush.radialGradient(listOf(style.beadLight, style.beadMid, style.beadDark), center = light, radius = r * 1.7f),
        radius = r,
        center = center,
    )
    drawCircle(Color.White.copy(alpha = 0.22f), radius = r * 0.16f, center = Offset(center.x - r * 0.42f, center.y - r * 0.42f))
}

/** Bead-material presets shown in the picker. First entry tracks the app theme primary. */
private fun beadMaterials(primary: Color): List<Pair<String, TasbihStyle>> = listOf(
    "Theme" to TasbihStyle(beadLight = lerp(primary, Color.White, 0.45f), beadMid = primary, beadDark = lerp(primary, Color.Black, 0.45f), cord = primary),
    "Sandal" to TasbihStyle(beadLight = Color(0xFFE7C896), beadMid = Color(0xFFA9783F), beadDark = Color(0xFF5C3A1E), cord = Color(0xFF7A5532)),
    "Pearl" to TasbihStyle(beadLight = Color(0xFFFFFFFF), beadMid = Color(0xFFE8E4DA), beadDark = Color(0xFFB4AD9E), cord = Color(0xFFCFC8BA)),
    "Onyx" to TasbihStyle(beadLight = Color(0xFF6B6B72), beadMid = Color(0xFF2E2E33), beadDark = Color(0xFF101013), cord = Color(0xFF3A3A40)),
    "Emerald" to TasbihStyle(beadLight = Color(0xFF6FE0A8), beadMid = Color(0xFF1E9E63), beadDark = Color(0xFF0C5436), cord = Color(0xFF14794B)),
    "Amber" to TasbihStyle(beadLight = Color(0xFFFFE08A), beadMid = Color(0xFFE0A52E), beadDark = Color(0xFF8A5E10), cord = Color(0xFFB9821F)),
    "Amethyst" to TasbihStyle(beadLight = Color(0xFFC9A8F0), beadMid = Color(0xFF8B5FD6), beadDark = Color(0xFF4A2E83), cord = Color(0xFF6B459E)),
)

/** Horizontal strip of tappable bead swatches at the bottom of the screen. */
@Composable
private fun MaterialPicker(
    materials: List<Pair<String, TasbihStyle>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    Row(
        modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        materials.forEachIndexed { i, (name, st) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(46.dp)
                        .border(
                            width = if (i == selected) 2.dp else 1.dp,
                            color = if (i == selected) c.primary else c.onSurfaceVariant.copy(alpha = 0.3f),
                            shape = CircleShape,
                        )
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(st.beadLight, st.beadMid, st.beadDark)))
                        .clickable { onSelect(i) },
                )
                Spacer(Modifier.height(6.dp))
                Text(name, fontSize = 11.sp, color = if (i == selected) c.primary else c.onSurfaceVariant)
            }
        }
    }
}
