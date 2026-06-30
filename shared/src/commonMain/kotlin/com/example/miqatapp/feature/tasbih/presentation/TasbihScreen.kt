package com.example.miqatapp.feature.tasbih.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Palette
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.widgets.AppBottomSheet
import com.example.miqatapp.core.widgets.AppButton
import com.example.miqatapp.core.widgets.AppCard
import com.example.miqatapp.core.widgets.LocalOverlay
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.tasbih
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

/** Bead silhouette. Oval keeps the same tangential width (= beadR) so the ring math never changes. */
internal enum class BeadShape { Round, Oval }

/** Surface treatment: plain gloss or marble veins with a centre star inlay. */
internal enum class BeadFinish { Glossy, Marble }

/** Look & feel of the misbaha. */
internal data class TasbihStyle(
    val beadDiameter: Dp = 44.dp,
    val beadLight: Color = Color(0xFFE7C896), // top-left highlight of the 3D bead
    val beadMid: Color = Color(0xFFA9783F),
    val beadDark: Color = Color(0xFF5C3A1E), // outer shadow
    val cord: Color = Color(0xFF7A5532),
    val gapBeads: Int = 4, // beads hidden in the "fingers" gap
    val shape: BeadShape = BeadShape.Round,
    val finish: BeadFinish = BeadFinish.Glossy,
)

/** The three counting styles, swapped from the Customize sheet. */
private enum class TasbihMode(val label: String) { Beads("Beads"), Tap("Tap"), Focus("Focus") }

// Ring placement on screen (from the Figma frame) — fixed, not user-facing.
private const val RADIUS_FACTOR = 0.6279f
private const val CENTER_X_FACTOR = 1.08f
private const val CENTER_Y_FACTOR = 1.014f
private const val RING_SLIDE_MS = 200 // settle animation after a swipe
private const val DRAG_DISTANCE = 3f // higher = a longer finger slide needed to move one bead

/** One completed dhikr, kept in History. ponytail: in-memory only — persist (DataStore/Room) later. */
private data class TasbihSession(val title: String, val target: Int, val date: LocalDate)

/**
 * Tasbih shell: owns the counting state, the chrome (app bar + dhikr card + sheets) and `tick()`.
 * The counting surface itself is swappable — [BeadCounter], [TapCounter] or [FocusCounter] — chosen
 * from the Customize sheet. Each body is self-contained and just calls back `onCount`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihScreen(onBack: () -> Unit = {}) {
    val haptics = LocalHapticFeedback.current

    val primary = AppTheme.colors.primary
    val materials = remember(primary) { beadMaterials(primary) }
    var selectedMaterial by remember { mutableIntStateOf(0) }
    // bead styling axes — capped sizes so a bead can never grow off-screen
    val beadSizes = remember { listOf(36.dp, 44.dp, 54.dp) } // S · M · L
    var sizeIdx by remember { mutableIntStateOf(1) }
    var shape by remember { mutableStateOf(BeadShape.Round) }
    var finish by remember { mutableStateOf(BeadFinish.Glossy) }
    val cfg = materials[selectedMaterial].second.copy(beadDiameter = beadSizes[sizeIdx], shape = shape, finish = finish)

    // the set handed off from the hub; fall back to a single default if opened directly. Mutable so the
    // active dhikr's target can be e
    // dited via the pen.
    val queue = remember { TasbihRun.queue.ifEmpty { listOf(Zikr("subhanallah", "SubhanAllah", "سُبْحَانَ ٱللَّٰه", 33, ZikrCategory.Tasbihat) to 33) }.toMutableStateList() }

    // counter state
    var index by remember { mutableIntStateOf(0) }   // current item in the set
    var count by remember { mutableIntStateOf(0) }   // count toward the current target
    var round by remember { mutableIntStateOf(0) }   // items completed this session
    var total by remember { mutableIntStateOf(0) }   // beads counted this session
    var paused by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }   // whole set finished
    var mode by remember { mutableStateOf(TasbihMode.Beads) }
    var vibrate by remember { mutableStateOf(true) }
    var sound by remember { mutableStateOf(false) } // ponytail: toggle only — no audio player wired yet
    var showHistory by remember { mutableStateOf(false) }
    var showEditCount by remember { mutableStateOf(false) }
    var showCustomize by remember { mutableStateOf(false) }
    val history = remember { mutableListOf<TasbihSession>().toMutableStateList() }
    val curZikr = queue[index].first
    val curTarget = queue[index].second

    fun tick() { // one bead counted
        total++
        val target = queue[index].second
        if (target > 0 && count + 1 >= target) {              // target reached (0 = unlimited → never auto-completes)
            round++
            history.add(0, TasbihSession(queue[index].first.title, target, currentDate()))
            if (vibrate) haptics.performHapticFeedback(HapticFeedbackType.LongPress) // heavier on a completed dhikr
            if (index < queue.lastIndex) { index++; count = 0 } // auto-advance to the next dhikr in the set
            else { count = target; done = true }                // whole set finished → success
        } else {
            count++
            if (vibrate) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    // Don't let the drawer's edge-swipe-to-open fight the counting gestures while on this screen.
    val overlay = LocalOverlay.current
    DisposableEffect(Unit) {
        overlay.drawerGesturesEnabled = false
        onDispose { overlay.drawerGesturesEnabled = true }
    }

    Scaffold(
        containerColor = AppTheme.colors.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.tasbih), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Lucide.ArrowLeft, "Back") }
                },
                actions = {
                    IconButton(onClick = { showCustomize = true }) { Icon(Lucide.Palette, "Counting style") }
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
            // swappable counting surface (fills the screen, sits under the card); crossfade so swapping
            // styles fades smoothly instead of hard-cutting between very different visuals.
            val enabled = !paused && !done
            Crossfade(targetState = mode, animationSpec = tween(350), label = "mode") { m ->
                when (m) {
                    TasbihMode.Beads -> BeadCounter(cfg = cfg, enabled = enabled, onCount = { tick() })
                    TasbihMode.Tap -> TapCounter(count = count, target = curTarget, enabled = enabled, onCount = { tick() })
                    TasbihMode.Focus -> FocusCounter(count = count, target = curTarget, enabled = enabled, onCount = { tick() })
                }
            }

            // card drawn AFTER the body so its action icons receive taps
            DhikrHeader(
                arabic = curZikr.arabic,
                translit = curZikr.title,
                target = curTarget,
                count = count,
                paused = paused,
                round = round,
                total = total,
                onEdit = { showEditCount = true },
                onPause = { paused = !paused },
                onHistory = { showHistory = true },
                onReset = { count = 0; round = 0; index = 0; done = false },
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
            )

            if (done) {
                SuccessOverlay(total = total) { done = false; index = 0; count = 0; round = 0 }
            }
        }
    }

    if (showHistory) {
        HistorySheet(history, onClear = { history.clear() }, onDismiss = { showHistory = false })
    }

    if (showEditCount) { // edit the active dhikr's target — reuses the hub's picker
        CountSheet(current = curTarget, onPick = { queue[index] = curZikr to it }, onDismiss = { showEditCount = false })
    }

    if (showCustomize) {
        CustomizeSheet(
            mode = mode, onMode = { mode = it },
            vibrate = vibrate, onVibrate = { vibrate = it },
            sound = sound, onSound = { sound = it },
            sizeIdx = sizeIdx, onSize = { sizeIdx = it },
            shape = shape, onShape = { shape = it },
            finish = finish, onFinish = { finish = it },
            materials = materials, selectedMaterial = selectedMaterial, onMaterial = { selectedMaterial = it },
            onDismiss = { showCustomize = false },
        )
    }
}

// ---- counting surfaces -------------------------------------------------------------------------

/**
 * Misbaha as a big bead ring — only the lower-left arc shows. Dragging rotates the ring one bead
 * (reversible) while one bead crosses the fixed gap; releasing past halfway counts +1 and settles.
 */
@Composable
private fun BeadCounter(cfg: TasbihStyle, enabled: Boolean, onCount: () -> Unit, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val beadR = with(density) { cfg.beadDiameter.toPx() / 2f }
    val gapHalf = cfg.gapBeads / 2f

    var angle by remember { mutableFloatStateOf(0f) }  // committed ring rotation (radians)
    var pluck by remember { mutableFloatStateOf(0f) }  // 0..1 progress of the current swipe
    var crossing by remember { mutableIntStateOf(-1) } // index of the bead crossing the gap (-1 = none)
    var crossTravel by remember { mutableIntStateOf(1) }

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

    Canvas(
        // re-key on beadR/gapHalf so a size change restarts the gesture with fresh geometry (drag stays in sync with the drawn beads)
        modifier.fillMaxSize().pointerInput(enabled, beadR, gapHalf) {
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
                    if (pluck > 0.5f && enabled) {
                        onCount()
                        scope.launch {
                            animate(pluck, 1f, animationSpec = tween(RING_SLIDE_MS)) { v, _ -> pluck = v }
                            angle += step // commit one-bead advance (ring angle stays continuous → no jump)
                            crossing = -1
                            pluck = 0f
                        }
                    } else {
                        scope.launch {
                            animate(pluck, 0f, animationSpec = tween(250)) { v, _ -> pluck = v } // reverse (didn't pass / disabled)
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
            bead(p, beadR, a, cfg)
        }
        if (crossing >= 0) {
            val start = crossing * step + angle
            val end = (crossing + crossTravel) * step + angle
            val ca = start + pluck * (end - start)
            bead(Offset(cen.x + r * cos(ca), cen.y + r * sin(ca)), beadR, ca, cfg)
        }
    }
}

/** Basic counter: one big progress-ring button, tap anywhere inside to count. */
@Composable
private fun TapCounter(count: Int, target: Int, enabled: Boolean, onCount: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val frac by animateFloatAsState(if (target > 0) (count.toFloat() / target).coerceIn(0f, 1f) else 0f, label = "tap")
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(250.dp).clip(CircleShape)
                .clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCount),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val sw = 14.dp.toPx()
                drawCircle(c.primary.copy(alpha = 0.12f), radius = (size.minDimension - sw) / 2f, style = Stroke(sw))
                drawArc(
                    color = c.primary, startAngle = -90f, sweepAngle = frac * 360f, useCenter = false,
                    topLeft = Offset(sw / 2f, sw / 2f), size = Size(size.width - sw, size.height - sw),
                    style = Stroke(sw, cap = StrokeCap.Round),
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$count", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                Text(if (target > 0) "/ $target" else "∞", fontSize = 14.sp, color = c.onSurfaceVariant)
            }
        }
    }
}

/** Meditative counter: a softly breathing orb; tap anywhere to count, rim fills toward the target. */
@Composable
private fun FocusCounter(count: Int, target: Int, enabled: Boolean, onCount: () -> Unit, modifier: Modifier = Modifier) {
    val c = AppTheme.colors
    val breath = rememberInfiniteTransition(label = "breath")
    val scale by breath.animateFloat(
        initialValue = 0.9f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(2600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale",
    )
    val frac by animateFloatAsState(if (target > 0) (count.toFloat() / target).coerceIn(0f, 1f) else 0f, label = "focus")
    Box(
        modifier.fillMaxSize().clickable(enabled = enabled, interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onCount),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(240.dp).scale(scale), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val sw = 6.dp.toPx()
                drawCircle(c.primary.copy(alpha = 0.10f), radius = size.minDimension / 2f - sw)
                drawCircle(c.primary.copy(alpha = 0.18f), radius = size.minDimension / 2f - sw / 2f, style = Stroke(sw))
                drawArc(
                    color = c.primary, startAngle = -90f, sweepAngle = frac * 360f, useCenter = false,
                    topLeft = Offset(sw / 2f, sw / 2f), size = Size(size.width - sw, size.height - sw),
                    style = Stroke(sw, cap = StrokeCap.Round),
                )
            }
            Text("$count", fontSize = 44.sp, fontWeight = FontWeight.Bold, color = c.primary)
        }
    }
}

// ---- chrome ------------------------------------------------------------------------------------

/** Compact 2-row counter card: counter · azkar on top, stats · actions below. */
@Composable
private fun DhikrHeader(
    arabic: String,
    translit: String,
    target: Int,
    count: Int,
    paused: Boolean,
    round: Int,
    total: Int,
    onEdit: () -> Unit,
    onPause: () -> Unit,
    onHistory: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    AppCard(modifier.fillMaxWidth()) {
        // row 1 — counter (left) · azkar (right)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$count", fontSize = 46.sp, fontWeight = FontWeight.Bold, color = if (paused) c.onSurfaceVariant else c.primary)
                Spacer(Modifier.width(4.dp))
                Text(
                    if (paused) "paused" else if (target <= 0) "∞" else "/ $target",
                    fontSize = 14.sp, color = c.onSurfaceVariant, modifier = Modifier.padding(bottom = 7.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(arabic, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
                Text(translit, fontSize = 12.sp, color = c.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        // row 2 — stats (left) · actions (right)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Round $round  ·  Total $total", fontSize = 12.sp, color = c.onSurfaceVariant)
            Row {
                CardAction(Lucide.Pencil, "Edit count", onEdit)
                CardAction(if (paused) Lucide.Play else Lucide.Pause, if (paused) "Resume" else "Pause", onPause)
                CardAction(Lucide.History, "History", onHistory)
                CardAction(Lucide.RotateCcw, "Reset", onReset)
            }
        }
    }
}

@Composable
private fun CardAction(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(34.dp)) {
        Icon(icon, label, tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

/** Pick the counting style + per-mode config (vibration, sound, and the full bead styling for Beads). */
@Composable
private fun CustomizeSheet(
    mode: TasbihMode,
    onMode: (TasbihMode) -> Unit,
    vibrate: Boolean,
    onVibrate: (Boolean) -> Unit,
    sound: Boolean,
    onSound: (Boolean) -> Unit,
    sizeIdx: Int,
    onSize: (Int) -> Unit,
    shape: BeadShape,
    onShape: (BeadShape) -> Unit,
    finish: BeadFinish,
    onFinish: (BeadFinish) -> Unit,
    materials: List<Pair<String, TasbihStyle>>,
    selectedMaterial: Int,
    onMaterial: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    AppBottomSheet(onDismiss = onDismiss) {
        // animateContentSize so the height grows/shrinks smoothly when the Beads styling appears/disappears
        Column(Modifier.fillMaxWidth().animateContentSize()) {
            Text("Customize", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = c.onSurface)
            Spacer(Modifier.height(16.dp))
            Text("Counting style", fontSize = 13.sp, color = c.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TasbihMode.entries.forEach { m ->
                    ModeChip(m.label, m == mode, Modifier.weight(1f)) { onMode(m) }
                }
            }
            Spacer(Modifier.height(18.dp))
            ToggleRow("Vibration", vibrate, onVibrate)
            ToggleRow("Sound", sound, onSound)
            if (mode == TasbihMode.Beads) {
                Spacer(Modifier.height(14.dp))
                ChipRow("Size", listOf("S", "M", "L"), sizeIdx, onSize)
                Spacer(Modifier.height(12.dp))
                ChipRow("Shape", BeadShape.entries.map { it.name }, shape.ordinal) { onShape(BeadShape.entries[it]) }
                Spacer(Modifier.height(12.dp))
                ChipRow("Finish", BeadFinish.entries.map { it.name }, finish.ordinal) { onFinish(BeadFinish.entries[it]) }
                Spacer(Modifier.height(14.dp))
                Text("Color", fontSize = 13.sp, color = c.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                MaterialGrid(materials = materials, selected = selectedMaterial, onSelect = onMaterial)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

/** Label + a row of equal-width text chips. */
@Composable
private fun ChipRow(label: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Text(label, fontSize = 13.sp, color = AppTheme.colors.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, o -> ModeChip(o, i == selected, Modifier.weight(1f)) { onSelect(i) } }
    }
}

@Composable
private fun ModeChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    Box(
        modifier.clip(RoundedCornerShape(12.dp)).background(if (selected) c.primary else c.cardColor).clickable(onClick = onClick).padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, fontWeight = FontWeight.Bold, color = if (selected) c.onPrimary else c.onSurface) }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontSize = 15.sp, color = AppTheme.colors.onSurface, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
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

/**
 * A single 3D bead. [angleRad] is the radial direction (centre → bead) so Oval/Diamond elongate along
 * the cord — their tangential width stays `r`, leaving the ring's spacing math untouched.
 */
private fun DrawScope.bead(center: Offset, r: Float, angleRad: Float, style: TasbihStyle) {
    when (style.shape) {
        BeadShape.Round -> beadBody(center, r, r, style)
        BeadShape.Oval -> rotate(angleRad * (180f / PI.toFloat()), center) { beadBody(center, r * 1.3f, r, style) }
    }
}

private val GOLD = Color(0xFFE8C879)

/** Bead body in the local (rotated) frame: rx = radial half-extent, ry = tangential half-extent (= beadR). */
private fun DrawScope.beadBody(center: Offset, rx: Float, ry: Float, style: TasbihStyle) {
    val brush = Brush.radialGradient(
        listOf(style.beadLight, style.beadMid, style.beadDark),
        center = Offset(center.x - rx * 0.28f, center.y - ry * 0.32f),
        radius = maxOf(rx, ry) * 1.75f,
    )
    drawOval(brush, topLeft = Offset(center.x - rx, center.y - ry), size = Size(rx * 2f, ry * 2f))

    if (style.finish == BeadFinish.Marble) {
        // organic swirling veins + an 8-point gold star inlay
        drawPath(swirlPath(center, rx, ry, false), color = lerp(style.beadLight, Color.White, 0.3f).copy(alpha = 0.5f), style = Stroke(ry * 0.09f, cap = StrokeCap.Round))
        drawPath(swirlPath(center, rx, ry, true), color = style.beadDark.copy(alpha = 0.4f), style = Stroke(ry * 0.07f, cap = StrokeCap.Round))
        drawPath(starPath(center, ry * 0.44f, ry * 0.18f), color = GOLD.copy(alpha = 0.85f))
        drawCircle(lerp(GOLD, Color.White, 0.3f), radius = ry * 0.07f, center = center)
    }
    // specular highlight
    drawCircle(Color.White.copy(alpha = 0.24f), radius = ry * 0.16f, center = Offset(center.x - rx * 0.4f, center.y - ry * 0.42f))
}

/** An organic S-curve across the bead, for marble veining. */
private fun swirlPath(c: Offset, rx: Float, ry: Float, alt: Boolean): Path = Path().apply {
    if (!alt) {
        moveTo(c.x - rx * 0.7f, c.y - ry * 0.1f)
        cubicTo(c.x - rx * 0.2f, c.y - ry * 0.7f, c.x + rx * 0.3f, c.y + ry * 0.6f, c.x + rx * 0.75f, c.y + ry * 0.15f)
    } else {
        moveTo(c.x - rx * 0.6f, c.y + ry * 0.45f)
        cubicTo(c.x - rx * 0.1f, c.y - ry * 0.2f, c.x + rx * 0.2f, c.y - ry * 0.55f, c.x + rx * 0.6f, c.y - ry * 0.45f)
    }
}

/** 8-point star (16 alternating vertices). */
private fun starPath(c: Offset, outer: Float, inner: Float): Path = Path().apply {
    val pts = 8
    for (k in 0 until pts * 2) {
        val rad = if (k % 2 == 0) outer else inner
        val ang = (PI.toFloat() / pts) * k - PI.toFloat() / 2f
        val x = c.x + rad * cos(ang)
        val y = c.y + rad * sin(ang)
        if (k == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
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

/** Grid of tappable bead swatches — 4 per row, all visible at once (no scrolling). */
@Composable
private fun MaterialGrid(
    materials: List<Pair<String, TasbihStyle>>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    val cols = 4
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        materials.chunked(cols).forEachIndexed { rowIdx, row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEachIndexed { colIdx, (name, st) ->
                    val i = rowIdx * cols + colIdx
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
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
                repeat(cols - row.size) { Spacer(Modifier.weight(1f)) } // pad last row so columns stay aligned
            }
        }
    }
}
