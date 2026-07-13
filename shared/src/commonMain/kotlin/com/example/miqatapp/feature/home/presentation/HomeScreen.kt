package com.example.miqatapp.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.MoonStar
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.feature.home.presentation.components.PrayerSceneHeader
import com.example.miqatapp.core.enums.MiqatTimeStatus
import com.example.miqatapp.core.enums.PrayerTrackerStatus
import com.example.miqatapp.core.datetime.HijriMonth
import com.example.miqatapp.core.datetime.labelRes
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.debug.Debug
import kotlinx.datetime.LocalTime
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.core.components.SehriInfoSheet
import com.example.miqatapp.core.datetime.Now
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import com.example.miqatapp.feature.miqat.store.MiqatCalculationStore
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.location.LocationResolver
import com.example.miqatapp.core.location.LocationMoveSheet
import com.example.miqatapp.core.location.rememberGeoLocator
import androidx.compose.runtime.LaunchedEffect
import com.example.miqatapp.feature.miqat.domain.MiqatTime
import com.example.miqatapp.core.constants.Place
import androidx.compose.runtime.collectAsState
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.clear
import com.example.miqatapp.resources.day_streak
import com.example.miqatapp.resources.mark_prayer
import com.example.miqatapp.resources.recite_before_sleep_after_isha
import com.example.miqatapp.resources.surah_al_mulk
import com.example.miqatapp.resources.this_week
import com.example.miqatapp.resources.hijri_era
import com.example.miqatapp.resources.today
import com.example.miqatapp.resources.verse_of_the_day
import com.example.miqatapp.resources.week_days
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import com.example.miqatapp.core.enums.color
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppCard
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.PulseDot

private val ExpandedHeader = 380.dp
private val CollapsedHeader = 116.dp

@Composable
fun HomeScreen() {
    val place by LocationStore.activePlace.collectAsState()
    val timeFormat by SettingsStore.timeFormat.collectAsState()
    val calc by MiqatCalculationStore.calculation.collectAsState()

    // One clock drives the screen: real, or a pushed/fast debug clock (see Now). The times store follows its date.
    val clock by Now.now.collectAsState()
    val now = clock.time
    val today by MiqatTimesStore.today.collectAsState()

    // silent GPS check — never prompts
    val geo = rememberGeoLocator()
    var moveCandidate by remember { mutableStateOf<Place?>(null) }
    LaunchedEffect(Unit) {
        val fix = geo.current() ?: return@LaunchedEffect
        moveCandidate = LocationResolver.detectMove(LocationStore.activePlace.value, fix)
    }

    val dailyTimes = remember(today) { today.filter { it.miqat in Miqat.DAILY } }
    val prayerTimes = dailyTimes.filter { it.miqat.isPrayer }
    // current fard by window: every prayer runs to the next EXCEPT Fajr, which ends at sunrise — so
    // sunrise→Dhuhr is a real fard-free gap (null). Wraps overnight (before Fajr → yesterday's Isha).
    val sunriseTime = today.firstOrNull { it.miqat == Miqat.Sunrise }?.at?.time
    val startedPrayer = prayerTimes.lastOrNull { it.at.time <= now } ?: prayerTimes.lastOrNull()
    val currentPrayer = when {
        startedPrayer == null -> null
        startedPrayer.miqat == Miqat.Fajr && sunriseTime != null && now >= sunriseTime -> null
        else -> startedPrayer.miqat
    }
    val nextMt = prayerTimes.firstOrNull { it.at.time > now } ?: prayerTimes.firstOrNull()

    // live sun/moon position + the current daily period (curated markers, drives the scene status)
    val sky = remember(now, today) { liveSkyState(now, today) }
    val period = remember(now, today) { currentPeriod(now, today) }
    val hijri by SettingsStore.hijriDate.collectAsState()
    val dateLabel = "${stringResource(clock.date.dayOfWeek.labelRes)}, ${hijri.day} ${HijriMonth.of(hijri.month).label()} ${hijri.year} ${stringResource(Res.string.hijri_era)}"

    val tracked = remember { mutableStateMapOf<Miqat, PrayerTrackerStatus?>() }
    var sheetPrayer by remember { mutableStateOf<Miqat?>(null) }
    val total = Miqat.PRAYERS.size
    val prayedCount = Miqat.PRAYERS.count { tracked[it].let { s -> s != null && s != PrayerTrackerStatus.Missed } }

    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val rangePx = with(density) { (ExpandedHeader - CollapsedHeader).toPx() }
    val fraction = (scroll.value / rangePx).coerceIn(0f, 1f)
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    // live ticking countdown to the next prayer, wrapping past midnight
    val countdown = nextMt?.let {
        val secs = ((it.at.time.toSecondOfDay() - now.toSecondOfDay()) + 24 * 3600) % (24 * 3600)
        val h = secs / 3600; val m = (secs % 3600) / 60; val s = secs % 60
        if (h > 0) "in ${h}h ${m}m ${s}s" else "in ${m}m ${s}s"
    } ?: ""

    // Ramadan: Sehri (Fajr or Imsak, per user pref) + Iftar (Maghrib) on the scene. Debug.FORCE_RAMADAN previews off-season.
    val sehriRef by SettingsStore.sehriReference.collectAsState()
    val ramadan = Debug.FORCE_RAMADAN || hijri.month == 9
    val sehri = if (ramadan) today.firstOrNull { it.miqat == sehriRef }?.at?.time?.format(timeFormat.pattern) else null
    val iftar = if (ramadan) today.firstOrNull { it.miqat == Miqat.Maghrib }?.at?.time?.format(timeFormat.pattern) else null
    var showSehriInfo by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Spacer(Modifier.height(ExpandedHeader))
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StreakCard(today = prayedCount, total = total, streak = 12, best = 21, onTimePct = 85)
                AppTileGroup(
                    title = stringResource(Res.string.today),
                    items = dailyTimes.map { mt ->
                        val status = when (mt.miqat) {
                            currentPrayer -> MiqatTimeStatus.Current
                            nextMt?.miqat -> MiqatTimeStatus.Soon
                            else -> null
                        }
                        AppTileItem(
                            title = mt.miqat.label(clock.date),
                            subtitle = mt.at.time.format(timeFormat.pattern),
                            selected = status == MiqatTimeStatus.Current,
                            leadingIcon = mt.miqat.icon,
                            leadingColor = AppTheme.colors.primary,
                            badge = if (status == MiqatTimeStatus.Current) {
                                { PulseDot(color = AppTheme.colors.primary) }
                            } else null,
                            trailing = if (mt.miqat.isPrayer) {
                                {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (status == MiqatTimeStatus.Soon) {
                                            Text(MiqatTimeStatus.Soon.label, color = MiqatTimeStatus.Soon.color, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                        }
                                        TrackControl(tracked[mt.miqat])
                                    }
                                }
                            } else null,
                            onClick = if (mt.miqat.isPrayer) { { sheetPrayer = mt.miqat } } else null,
                        )
                    },
                )
                MulkReminderCard()
                DailyVerseCard()
                Spacer(Modifier.height(8.dp))
            }
        }

        PrayerSceneHeader(
            prayer = nextMt?.miqat ?: Miqat.Fajr,
            period = period,
            sky = sky,
            fraction = fraction,
            locationName = place.name,
            dateLabel = dateLabel,
            nextTime = nextMt?.at?.time?.format(timeFormat.pattern) ?: "",
            countdown = countdown,
            sehri = sehri,
            iftar = iftar,
            onInfo = { showSehriInfo = true },
            expandedHeight = ExpandedHeader,
            collapsedHeight = CollapsedHeader,
            onMenuClick = { scope.launch { drawerState.open() } },
        )

        if (showSehriInfo) SehriInfoSheet(onDismiss = { showSehriInfo = false })

        sheetPrayer?.let { p ->
            TrackingSheet(
                prayer = p,
                current = tracked[p],
                onSelect = { tracked[p] = it; sheetPrayer = null },
                onDismiss = { sheetPrayer = null },
            )
        }

        moveCandidate?.let { cand ->
            // show only when the method actually changes
            val newMethod = CalculationMethod.forCountry(cand.countryCode)
            val methodChange = if (newMethod != calc.method) calc.method to newMethod else null
            LocationMoveSheet(
                candidate = cand,
                current = place,
                methodChange = methodChange,
                onUpdate = { switchMethod ->
                    LocationStore.setActive(cand)
                    if (switchMethod) MiqatCalculationStore.setMethod(newMethod)
                    moveCandidate = null
                },
                onKeep = { moveCandidate = null },
            )
        }

        // debug readout — shown only with the fast clock: confirms scene period + current/next stay in sync.
        if (Debug.FAST_CLOCK) {
            Text(
                "${clock.date}  " + now.hour.toString().padStart(2, '0') + ":" + now.minute.toString().padStart(2, '0') +
                    "   " + period.name + "   cur:" + (currentPrayer?.name ?: "—") + " next:" + (nextMt?.miqat?.name ?: "—"),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

// curated markers for the header's "Now" line (names the sunrise→Dhuhr gap too — Sunrise/Ishraq).
private val PERIOD_MARKERS = listOf(Miqat.Fajr, Miqat.Sunrise, Miqat.Ishraq, Miqat.Dhuhr, Miqat.Asr, Miqat.Maghrib, Miqat.Isha)

/** The daily marker we're in now. Wraps overnight to Isha (before Fajr → yesterday's), so it's never blank. */
private fun currentPeriod(now: LocalTime, times: List<MiqatTime>): Miqat {
    val pts = PERIOD_MARKERS
        .mapNotNull { m -> times.firstOrNull { it.miqat == m }?.let { m to (it.at.time.hour * 60 + it.at.time.minute) } }
        .sortedBy { it.second }
    if (pts.isEmpty()) return Miqat.Isha
    val n = now.hour * 60 + now.minute
    val idx = pts.indexOfLast { it.second <= n }
    return if (idx >= 0) pts[idx].first else pts.last().first
}


@Composable
private fun TrackControl(tracked: PrayerTrackerStatus?) {
    if (tracked != null) {
        val sc = tracked.color
        Box(Modifier.size(32.dp).clip(CircleShape).background(sc.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
            Icon(tracked.icon, tracked.label, tint = sc, modifier = Modifier.size(18.dp))
        }
    } else {
        Box(Modifier.size(32.dp).clip(CircleShape).border(1.dp, AppTheme.colors.outlineVariant, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Lucide.Plus, "Track", tint = AppTheme.colors.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TrackingSheet(
    prayer: Miqat,
    current: PrayerTrackerStatus?,
    onSelect: (PrayerTrackerStatus?) -> Unit,
    onDismiss: () -> Unit,
) {
    AppBottomSheet(onDismiss = onDismiss) {
        Text(
            stringResource(Res.string.mark_prayer, prayer.name), color = AppTheme.colors.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
        )
        AppTileGroup(
            items = PrayerTrackerStatus.entries.map { st ->
                val sc = st.color
                AppTileItem(
                    title = st.label,
                    selected = st == current,
                    leadingIcon = st.icon,
                    leadingColor = sc,
                    trailing = if (st == current) { { Icon(Lucide.Check, null, tint = sc, modifier = Modifier.size(20.dp)) } } else null,
                    onClick = { onSelect(st) },
                )
            },
        )
        if (current != null) {
            AppTile(
                title = stringResource(Res.string.clear),
                leadingIcon = Lucide.X,
                leadingColor = AppTheme.colors.onSurfaceVariant,
                onClick = { onSelect(null) },
            )
        }
    }
}

@Composable
private fun StreakCard(today: Int, total: Int, streak: Int, best: Int, onTimePct: Int) {
    AppCard(padding = 18.dp, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
             Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { today / total.toFloat() },
                    modifier = Modifier.size(72.dp),
                    strokeWidth = 7.dp,
                    color = AppTheme.colors.surfaceTint,
                    trackColor = AppTheme.colors.neutralMutedContainer,
                )
                Text("$today/$total", color = AppTheme.colors.onSurface, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Lucide.Flame, null, tint = AppTheme.colors.warning, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("$streak", color = AppTheme.colors.onSurface, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.day_streak), color = AppTheme.colors.onSurfaceVariant, fontSize = 13.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text("Best $best days  ·  $onTimePct% on time", color = AppTheme.colors.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(Res.string.this_week), color = AppTheme.colors.onSurfaceVariant, fontSize = 11.sp)
            val days = stringArrayResource(Res.array.week_days)
            val levels = listOf(2, 2, 1, 2, 2, 0, 2)
            val todayIndex = 4
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEachIndexed { i, d ->
                    val isToday = i == todayIndex
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DayDot(levels[i])
                        Text(
                            d,
                            color = if (isToday) AppTheme.colors.primary else AppTheme.colors.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        )
                    }
                }
            }
        }
    }
}

/** Day status dot: full = check, partial = dash, none/upcoming = empty outline. */
@Composable
private fun DayDot(level: Int) {
    val c = AppTheme.colors
    when (level) {
        2 -> Box(Modifier.size(34.dp).clip(CircleShape).background(c.success.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
            Icon(Lucide.Check, null, tint = c.success, modifier = Modifier.size(18.dp))
        }
        1 -> Box(Modifier.size(34.dp).clip(CircleShape).background(c.warning.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
            Icon(Lucide.Minus, null, tint = c.warning, modifier = Modifier.size(18.dp))
        }
        else -> Box(Modifier.size(34.dp).clip(CircleShape).border(1.5.dp, c.outlineVariant, CircleShape))
    }
}

@Composable
private fun MulkReminderCard() {
    AppTile(
        title = stringResource(Res.string.surah_al_mulk),
        subtitle = stringResource(Res.string.recite_before_sleep_after_isha),
        leadingIcon = Lucide.MoonStar,
        leadingColor = AppTheme.colors.primary,
        trailing = { Box(Modifier.size(8.dp).clip(CircleShape).background(AppTheme.colors.success)) },
    )
}

@Composable
private fun DailyVerseCard() {
    AppCard(padding = 18.dp, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Lucide.BookOpen, null, tint = AppTheme.colors.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(Res.string.verse_of_the_day), color = AppTheme.colors.onSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
        Text("\"Indeed, prayer prohibits immorality and wrongdoing.\"", color = AppTheme.colors.onSurface, fontSize = 15.sp)
        Text("— Surah Al-'Ankabut 29:45", color = AppTheme.colors.onSurfaceVariant, fontSize = 12.sp)
    }
}


