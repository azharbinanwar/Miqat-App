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
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Palette
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.widgets.AppBottomSheet
import com.example.miqatapp.core.widgets.AppButton
import com.example.miqatapp.core.widgets.AppTileGroup
import com.example.miqatapp.core.widgets.AppTileItem
import com.example.miqatapp.core.widgets.AppCard
import com.example.miqatapp.core.widgets.LocalOverlay
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.reset
import com.example.miqatapp.resources.tasbih
import com.example.miqatapp.resources.tasbih_adhkar_complete
import com.example.miqatapp.resources.tasbih_back
import com.example.miqatapp.resources.tasbih_color
import com.example.miqatapp.resources.tasbih_counting_style
import com.example.miqatapp.resources.tasbih_customize
import com.example.miqatapp.resources.tasbih_dhikr_complete
import com.example.miqatapp.resources.tasbih_done
import com.example.miqatapp.resources.tasbih_edit_count
import com.example.miqatapp.resources.tasbih_finish
import com.example.miqatapp.resources.tasbih_finish_glossy
import com.example.miqatapp.resources.tasbih_finish_marble
import com.example.miqatapp.resources.tasbih_history
import com.example.miqatapp.resources.tasbih_in_time_mashaallah
import com.example.miqatapp.resources.tasbih_mode_beads
import com.example.miqatapp.resources.tasbih_mode_focus
import com.example.miqatapp.resources.tasbih_mode_tap
import com.example.miqatapp.resources.tasbih_pause
import com.example.miqatapp.resources.tasbih_paused
import com.example.miqatapp.resources.tasbih_repeat
import com.example.miqatapp.resources.tasbih_repeat_set
import com.example.miqatapp.resources.tasbih_resume
import com.example.miqatapp.resources.tasbih_round_total
import com.example.miqatapp.resources.tasbih_shape
import com.example.miqatapp.resources.tasbih_shape_oval
import com.example.miqatapp.resources.tasbih_shape_round
import com.example.miqatapp.resources.tasbih_size
import com.example.miqatapp.resources.tasbih_sound
import com.example.miqatapp.resources.tasbih_vibration
import com.example.miqatapp.resources.total
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.time.TimeMark
import kotlin.time.TimeSource
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
fun TasbihScreen(
    zikrs: List<Zikr> = emptyList(), // 1 or many — the screen doesn't care; each zikr carries its own target in [Zikr.defaultCount]
    onBack: () -> Unit = {},
    onHistory: (String) -> Unit = {},
) {
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

    // the set passed in by the caller; fall back to a single default if opened with nothing.
    // Mutable so the active dhikr's target can be edited via the pen.
    val queue = remember { zikrs.ifEmpty { listOf(Zikr("subhanallah", "SubhanAllah", "سُبْحَانَ ٱللَّٰه", 33, ZikrCategory.Tasbihat)) }.toMutableStateList() }

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
    var showEditCount by remember { mutableStateOf(false) }
    var showCustomize by remember { mutableStateOf(false) }
    var startMark by remember { mutableStateOf<TimeMark?>(null) } // set on the first bead, for the Done-screen time
    var elapsedSec by remember { mutableIntStateOf(0) }
    val history = remember { mutableListOf<TasbihSession>().toMutableStateList() }
    val curZikr = queue[index]
    val curTarget = queue[index].defaultCount

    fun tick() { // one bead counted
        if (total == 0) startMark = TimeSource.Monotonic.markNow()
        total++
        val target = queue[index].defaultCount
        if (target > 0 && count + 1 >= target) {              // target reached (0 = unlimited → never auto-completes)
            round++
            history.add(0, TasbihSession(queue[index].title, target, currentDate()))
            if (vibrate) haptics.performHapticFeedback(HapticFeedbackType.LongPress) // heavier on a completed dhikr
            if (index < queue.lastIndex) { index++; count = 0 } // auto-advance to the next dhikr in the set
            else { // whole set finished → success
                count = target
                elapsedSec = startMark?.elapsedNow()?.inWholeSeconds?.toInt() ?: 0
                done = true
            }
        } else {
            count++
            if (vibrate) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    fun restart() { done = false; index = 0; count = 0; round = 0; total = 0; startMark = null }

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
                    IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.tasbih_back)) }
                },
                actions = {
                    IconButton(onClick = { onHistory(curZikr.id) }) { Icon(Lucide.History, stringResource(Res.string.tasbih_history)) }
                    IconButton(onClick = { restart() }) { Icon(Lucide.RotateCcw, stringResource(Res.string.reset)) }
                    IconButton(onClick = { showCustomize = true }) { Icon(Lucide.Palette, stringResource(Res.string.tasbih_counting_style)) }
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
                queue = queue,
                index = index,
                count = count,
                paused = paused,
                round = round,
                total = total,
                onEdit = { showEditCount = true },
                onPause = { paused = !paused },
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
            )

            if (done) {
                TasbihSuccessSheet(
                    items = queue.map { it.title to it.defaultCount },
                    total = total,
                    elapsedSec = elapsedSec,
                    onRepeat = { restart() },
                    onDone = onBack,
                    onDismiss = { restart() },
                )
            }
        }
    }

    if (showEditCount) { // edit the active dhikr's target — reuses the hub's picker
        CountSheet(current = curTarget, onPick = { queue[index] = curZikr.copy(defaultCount = it) }, onDismiss = { showEditCount = false })
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

/**
 * Slim counter card — one stat row (count/target · round · total, actions right) plus the dhikr
 * text capped at two lines. The text zone is height-stable across short/long items so the
 * counting surface below never reflows mid-session; every dp saved here goes to the tap zone.
 */
@Composable
private fun DhikrHeader(
    queue: List<Zikr>, // the whole set — works the same for 1 or many items
    index: Int,
    count: Int,
    paused: Boolean,
    round: Int,
    total: Int,
    onEdit: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = AppTheme.colors
    // everything display-related is derived from the queue itself
    val arabic = queue[index].arabic
    val target = queue[index].defaultCount
    val expectedRounds = queue.size
    // grand total only makes sense when every item has a finite target (0 = unlimited)
    val grandTotal = if (queue.all { it.defaultCount > 0 }) queue.sumOf { it.defaultCount } else 0
    AppCard(modifier.fillMaxWidth()) {
        // arabic on top — regular weight, space for 2 lines always reserved so the card never jumps
        Text(
            arabic, fontSize = 20.sp, color = c.onSurface,
            minLines = 2, maxLines = 2, overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End, lineHeight = 26.sp,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(4.dp))
        // bottom row — count (primary + bold) next to target and round·total, actions on the right
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Text("$count", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (paused) c.onSurfaceVariant else c.primary)
                Spacer(Modifier.width(4.dp))
                Text(
                    if (paused) stringResource(Res.string.tasbih_paused) else if (target <= 0) "∞" else "/ $target",
                    fontSize = 13.sp, color = c.onSurfaceVariant,
                )
                Spacer(Modifier.width(10.dp))
                // progress format: current/expected — e.g. Round 2/3 · Total 45/99
                val curRound = (round + 1).coerceAtMost(expectedRounds)
                Text(
                    stringResource(
                        Res.string.tasbih_round_total,
                        "$curRound/$expectedRounds",
                        if (grandTotal > 0) "$total/$grandTotal" else "$total",
                    ),
                    fontSize = 13.sp, color = c.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            CardAction(Lucide.Pencil, stringResource(Res.string.tasbih_edit_count), onEdit)
            CardAction(if (paused) Lucide.Play else Lucide.Pause, if (paused) stringResource(Res.string.tasbih_resume) else stringResource(Res.string.tasbih_pause), onPause)
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
            Text(stringResource(Res.string.tasbih_customize), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = c.onSurface)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(Res.string.tasbih_counting_style), fontSize = 13.sp, color = c.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TasbihMode.entries.forEach { m ->
                    ModeChip(modeLabel(m), m == mode, Modifier.weight(1f)) { onMode(m) }
                }
            }
            Spacer(Modifier.height(18.dp))
            ToggleRow(stringResource(Res.string.tasbih_vibration), vibrate, onVibrate)
            ToggleRow(stringResource(Res.string.tasbih_sound), sound, onSound)
            if (mode == TasbihMode.Beads) {
                Spacer(Modifier.height(14.dp))
                ChipRow(stringResource(Res.string.tasbih_size), listOf("S", "M", "L"), sizeIdx, onSize)
                Spacer(Modifier.height(12.dp))
                ChipRow(stringResource(Res.string.tasbih_shape), BeadShape.entries.map { shapeLabel(it) }, shape.ordinal) { onShape(BeadShape.entries[it]) }
                Spacer(Modifier.height(12.dp))
                ChipRow(stringResource(Res.string.tasbih_finish), BeadFinish.entries.map { finishLabel(it) }, finish.ordinal) { onFinish(BeadFinish.entries[it]) }
                Spacer(Modifier.height(14.dp))
                Text(stringResource(Res.string.tasbih_color), fontSize = 13.sp, color = c.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                MaterialGrid(materials = materials, selected = selectedMaterial, onSelect = onMaterial)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

// Localized labels for the style enums — rendered here only; the enum names stay as stable ids.
@Composable
private fun modeLabel(mode: TasbihMode): String = stringResource(
    when (mode) {
        TasbihMode.Beads -> Res.string.tasbih_mode_beads
        TasbihMode.Tap -> Res.string.tasbih_mode_tap
        TasbihMode.Focus -> Res.string.tasbih_mode_focus
    },
)

@Composable
private fun shapeLabel(shape: BeadShape): String = stringResource(
    when (shape) {
        BeadShape.Round -> Res.string.tasbih_shape_round
        BeadShape.Oval -> Res.string.tasbih_shape_oval
    },
)

@Composable
private fun finishLabel(finish: BeadFinish): String = stringResource(
    when (finish) {
        BeadFinish.Glossy -> Res.string.tasbih_finish_glossy
        BeadFinish.Marble -> Res.string.tasbih_finish_marble
    },
)

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

/**
 * Success sheet shown when the run finishes. Adapts: a single dhikr shows just its count; a set shows
 * the per-dhikr breakdown via [AppTileGroup] with a Total tile. Scrolls if long; Done/Repeat are pinned
 * in the sheet footer so they stay reachable.
 */
@Composable
private fun TasbihSuccessSheet(
    items: List<Pair<String, Int>>,
    total: Int,
    elapsedSec: Int,
    onRepeat: () -> Unit,
    onDone: () -> Unit,
    onDismiss: () -> Unit,
) {
    val c = AppTheme.colors
    val single = items.size == 1
    val tiles = buildList {
        items.forEach { (title, n) -> add(AppTileItem(title = title, trailing = { Text("× $n", fontWeight = FontWeight.Bold, color = c.primary) })) }
        if (!single) add(AppTileItem(title = stringResource(Res.string.total), trailing = { Text("$total", fontWeight = FontWeight.Bold, color = c.primary) }))
    }
    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(stringResource(Res.string.tasbih_done), onClick = onDone, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRepeat, modifier = Modifier.fillMaxWidth()) { Text(if (single) stringResource(Res.string.tasbih_repeat) else stringResource(Res.string.tasbih_repeat_set)) }
        },
    ) {
        var shown by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { shown = true }
        val checkScale by animateFloatAsState(if (shown) 1f else 0.5f, animationSpec = spring(dampingRatio = 0.45f), label = "check")
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("✓", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = c.primary, modifier = Modifier.scale(checkScale))
            Spacer(Modifier.height(8.dp))
            Text(if (single) stringResource(Res.string.tasbih_dhikr_complete) else stringResource(Res.string.tasbih_adhkar_complete), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = c.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(Res.string.tasbih_in_time_mashaallah, formatDuration(elapsedSec)), fontSize = 13.sp, color = c.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
        }
        AppTileGroup(items = tiles, modifier = Modifier.fillMaxWidth())
    }
}

private fun formatDuration(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
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
