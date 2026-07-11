package com.example.miqatapp.feature.duas.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.MoonStar
import com.composables.icons.lucide.RotateCcw
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Sparkles
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.navigation.AppRoute
import com.example.miqatapp.core.navigation.LocalNavController
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.components.AppTextField
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.core.components.StateView
import com.example.miqatapp.feature.tasbih.presentation.TasbihRun
import com.example.miqatapp.feature.tasbih.presentation.Zikr
import com.example.miqatapp.feature.tasbih.presentation.ZikrCategory
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.add_your_own
import com.example.miqatapp.resources.added_by_you
import com.example.miqatapp.resources.after_prayer
import com.example.miqatapp.resources.arabic_text
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.collections
import com.example.miqatapp.resources.completed
import com.example.miqatapp.resources.dua_of_the_day
import com.example.miqatapp.resources.duas_and_adhkar
import com.example.miqatapp.resources.everyday_duas
import com.example.miqatapp.resources.evening
import com.example.miqatapp.resources.favorites
import com.example.miqatapp.resources.menu
import com.example.miqatapp.resources.morning
import com.example.miqatapp.resources.n_duas
import com.example.miqatapp.resources.no_duas_found
import com.example.miqatapp.resources.repetitions
import com.example.miqatapp.resources.reset
import com.example.miqatapp.resources.save
import com.example.miqatapp.resources.search_duas
import com.example.miqatapp.resources.start_on_tasbih
import com.example.miqatapp.resources.tasbihat
import com.example.miqatapp.resources.translation_optional
import com.example.miqatapp.resources.try_a_different_search
import com.example.miqatapp.resources.use_beads
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

// ───────────────────────── mock catalog (ponytail: replace with the Hisnul Muslim DB later) ─────────────────────────

/** One supplication. [repeat] = how many times it's said (1 / 3 / 7 / 33 / 34 / 100). Content is raw mock; chrome is localized. */
data class Dua(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val reference: String,
    val repeat: Int = 1,
)

/** The buckets people actually recognize (Hisnul Muslim, collapsed). Sections are ordered — After prayer / Morning / Evening are sunnah sequences. */
enum class DuaSection(val icon: ImageVector, val labelRes: StringResource) {
    Morning(Lucide.Sunrise, Res.string.morning),
    Evening(Lucide.Sunset, Res.string.evening),
    AfterPrayer(Lucide.Sparkles, Res.string.after_prayer),
    Everyday(Lucide.BookOpen, Res.string.everyday_duas),
    Tasbihat(Lucide.MoonStar, Res.string.tasbihat),
}

private val CATALOG: Map<DuaSection, List<Dua>> = mapOf(
    DuaSection.Morning to listOf(
        Dua("m1", "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Asbahna wa asbahal-mulku lillahi rabbil-'alamin",
            "We have reached the morning and at this time all sovereignty belongs to Allah, Lord of the worlds.", "Muslim 2723"),
        Dua("m2", "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا", "Allahumma bika asbahna wa bika amsayna",
            "O Allah, by You we enter the morning and by You we enter the evening.", "Tirmidhi 3391"),
        Dua("m3", "حَسْبِيَ اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ", "Hasbiyallahu la ilaha illa huwa 'alayhi tawakkalt",
            "Allah is sufficient for me; there is no god but Him. Upon Him I rely.", "Abu Dawud 5081", repeat = 7),
    ),
    DuaSection.Evening to listOf(
        Dua("e1", "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ رَبِّ الْعَالَمِينَ", "Amsayna wa amsal-mulku lillahi rabbil-'alamin",
            "We have reached the evening and at this time all sovereignty belongs to Allah, Lord of the worlds.", "Muslim 2723"),
        Dua("e2", "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ", "A'udhu bikalimatillahit-tammati min sharri ma khalaq",
            "I seek refuge in the perfect words of Allah from the evil of what He created.", "Muslim 2708", repeat = 3),
    ),
    // ordered exactly as prayed — do not sort when handing to Tasbih
    DuaSection.AfterPrayer to listOf(
        Dua("ap1", "أَسْتَغْفِرُ اللَّهَ", "Astaghfirullah",
            "I seek the forgiveness of Allah.", "Muslim 591", repeat = 3),
        Dua("ap2", "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ", "Allahumma antas-salamu wa minkas-salam, tabarakta ya dhal-jalali wal-ikram",
            "O Allah, You are Peace and from You is peace. Blessed are You, Owner of majesty and honour.", "Muslim 591"),
        Dua("ap3", "سُبْحَانَ اللَّهِ", "SubhanAllah", "Glory be to Allah.", "Muslim 596", repeat = 33),
        Dua("ap4", "الْحَمْدُ لِلَّهِ", "Alhamdulillah", "Praise be to Allah.", "Muslim 596", repeat = 33),
        Dua("ap5", "اللَّهُ أَكْبَرُ", "Allahu Akbar", "Allah is the Greatest.", "Muslim 596", repeat = 34),
        Dua("ap6", "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "La ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamd, wa huwa 'ala kulli shay'in qadir",
            "There is no god but Allah alone, with no partner. His is the dominion and the praise, and He is able to do all things.", "Muslim 596"),
    ),
    DuaSection.Everyday to listOf(
        Dua("d1", "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا", "Bismika Allahumma amutu wa ahya",
            "In Your name, O Allah, I die and I live.", "Bukhari 6324"),
        Dua("d2", "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا", "Alhamdulillahil-ladhi ahyana ba'da ma amatana",
            "Praise is to Allah who gave us life after He caused us to die.", "Bukhari 6324"),
        Dua("d3", "بِسْمِ اللَّهِ تَوَكَّلْتُ عَلَى اللَّهِ", "Bismillahi tawakkaltu 'alallah",
            "In the name of Allah, I place my trust in Allah.", "Abu Dawud 5095"),
        Dua("d4", "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا", "Subhanal-ladhi sakhkhara lana hadha",
            "Glory to Him who has subjected this to us.", "Muslim 1342"),
        Dua("d5", "لَا إِلَٰهَ إِلَّا أَنْتَ سُبْحَانَكَ إِنِّي كُنْتُ مِنَ الظَّالِمِينَ", "La ilaha illa anta subhanaka inni kuntu minaz-zalimin",
            "There is no god but You, glory to You. Indeed, I was of the wrongdoers.", "Al-Anbiya 21:87"),
        Dua("d6", "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ", "Allahumma inni a'udhu bika minal-hammi wal-hazan",
            "O Allah, I seek refuge in You from anxiety and grief.", "Bukhari 6369"),
    ),
    DuaSection.Tasbihat to listOf(
        Dua("t1", "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ", "SubhanAllahi wa bihamdih",
            "Glory is to Allah and praise is to Him.", "Muslim 2692", repeat = 100),
        Dua("t2", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", "La hawla wa la quwwata illa billah",
            "There is no might nor power except with Allah.", "Bukhari 6384", repeat = 100),
        Dua("t3", "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", "Astaghfirullaha wa atubu ilayh",
            "I seek the forgiveness of Allah and turn to Him in repentance.", "Muslim 2702", repeat = 100),
    ),
)

/** The featured dua on the hub. ponytail: fixed pick; rotate by day when the calendar wires in. */
private val DUA_OF_THE_DAY = Dua(
    "dotd", "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
    "Rabbana atina fid-dunya hasanatan wa fil-akhirati hasanatan wa qina 'adhaban-nar",
    "Our Lord, give us good in this world and good in the Hereafter, and protect us from the punishment of the Fire.",
    "Al-Baqarah 2:201",
)

/** Hearted duas + the user's own additions. ponytail: in-memory; move to DB with the rest. */
object DuaStore {
    val favIds = mutableStateListOf<String>()
    private val custom = mutableStateMapOf<DuaSection, MutableList<Dua>>()
    private var seq = 0

    fun customOf(section: DuaSection): List<Dua> = custom[section].orEmpty()
    fun allCustom(): List<Dua> = custom.values.flatten()
    fun add(section: DuaSection, dua: Dua) { custom.getOrPut(section) { mutableStateListOf() }.add(dua) }
    fun nextId(): String = "custom-${seq++}"
}

private fun allDuas() = CATALOG.values.flatten() + DuaStore.allCustom() + DUA_OF_THE_DAY

/** A dua becomes a one-item dhikr for the Tasbih counter — [repeat] is its target. Order is preserved by the caller. */
private fun Dua.toZikr() = Zikr(id, transliteration, arabic, repeat, ZikrCategory.Tasbihat)

// ───────────────────────────────────────── screen ─────────────────────────────────────────

@Composable
fun DuasScreen() {
    val nav = LocalNavController.current
    // the whole link with Tasbih: queue the duas (in order) and jump to the counter — reuses Tasbih's own hand-off
    val startOnTasbih: (List<Dua>) -> Unit = { duas ->
        if (duas.isNotEmpty()) {
            TasbihRun.queue = duas.map { it.toZikr() }
            nav.navigate(AppRoute.TasbihCounter)
        }
    }

    // in-screen nav: null = hub; non-null = the reader for a section / favorites / the featured dua
    var reader by remember { mutableStateOf<ReaderTarget?>(null) }
    reader?.let { target ->
        // "Add your own" only makes sense inside a section — custom duas land in that section
        val onAddCustom: ((Dua) -> Unit)? = (target as? ReaderTarget.Section)?.let { t -> { dua -> DuaStore.add(t.section, dua) } }
        DuaReaderScreen(target.title(), target.duas(), startOnTasbih, onAddCustom, onBack = { reader = null })
        return
    }
    DuasHub(onOpen = { reader = it }, onStartTasbih = startOnTasbih)
}

/** Reusable entry for the Azkar screen: open a collection ([section]) in the shared reader — same check/count/beads session. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarCollectionReader(section: DuaSection, onBack: () -> Unit) {
    val nav = LocalNavController.current
    val startOnTasbih: (List<Dua>) -> Unit = { duas ->
        if (duas.isNotEmpty()) {
            TasbihRun.queue = duas.map { it.toZikr() }
            nav.navigate(AppRoute.TasbihCounter)
        }
    }
    DuaReaderScreen(
        title = stringResource(section.labelRes),
        duas = CATALOG[section].orEmpty() + DuaStore.customOf(section),
        onStartTasbih = startOnTasbih,
        onAddCustom = { DuaStore.add(section, it) },
        onBack = onBack,
    )
}

/** Every azkar in a [section] — the pool for the collection picker. */
fun duasOf(section: DuaSection): List<Dua> = CATALOG[section].orEmpty()

/** Reader for an arbitrary list of azkar (e.g. a user-built collection). Same check/count/beads session. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarListReader(title: String, duas: List<Dua>, onBack: () -> Unit) {
    val nav = LocalNavController.current
    val startOnTasbih: (List<Dua>) -> Unit = { ds ->
        if (ds.isNotEmpty()) {
            TasbihRun.queue = ds.map { it.toZikr() }
            nav.navigate(AppRoute.TasbihCounter)
        }
    }
    DuaReaderScreen(title = title, duas = duas, onStartTasbih = startOnTasbih, onAddCustom = null, onBack = onBack)
}

/** Load [duas] into the Tasbih beads counter's queue — navigate to AppRoute.TasbihCounter after calling. */
fun queueForBeads(duas: List<Dua>) { TasbihRun.queue = duas.map { it.toZikr() } }

/** What the reader shows — a whole section, favorites, or just the featured dua. */
private sealed interface ReaderTarget {
    @Composable fun title(): String
    fun duas(): List<Dua>

    data class Section(val section: DuaSection) : ReaderTarget {
        @Composable override fun title() = stringResource(section.labelRes)
        override fun duas() = CATALOG[section].orEmpty() + DuaStore.customOf(section) // catalog (sunnah order) then the user's own
    }
    data object Favorites : ReaderTarget {
        @Composable override fun title() = stringResource(Res.string.favorites)
        override fun duas() = allDuas().filter { it.id in DuaStore.favIds }
    }
    data class Single(val dua: Dua) : ReaderTarget {
        @Composable override fun title() = stringResource(Res.string.dua_of_the_day)
        override fun duas() = listOf(dua)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuasHub(onOpen: (ReaderTarget) -> Unit, onStartTasbih: (List<Dua>) -> Unit) {
    val c = AppTheme.colors
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    val count = remember { mutableStateMapOf<String, Int>() } // tap-to-count also works from search results

    val q = query.trim()
    val results = if (q.isBlank()) emptyList() else allDuas().filter {
        it.transliteration.contains(q, true) || it.translation.contains(q, true) ||
            it.reference.contains(q, true) || it.arabic.contains(q)
    }

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.duas_and_adhkar), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, stringResource(Res.string.menu)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { AppTextField(value = query, onValueChange = { query = it }, placeholder = stringResource(Res.string.search_duas)) }

            if (q.isNotBlank()) {
                if (results.isEmpty()) {
                    item { StateView(title = stringResource(Res.string.no_duas_found), message = stringResource(Res.string.try_a_different_search), modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) }
                } else {
                    items(results, key = { it.id }) { dua ->
                        DuaCard(dua, count[dua.id] ?: 0, onAdvance = { advance(count, dua) }, onBeads = { onStartTasbih(listOf(dua)) })
                    }
                }
            } else {
                item { DuaOfTheDayCard(DUA_OF_THE_DAY) { onOpen(ReaderTarget.Single(DUA_OF_THE_DAY)) } }
                item {
                    val favCount = allDuas().count { it.id in DuaStore.favIds }
                    AppTileGroup(
                        title = stringResource(Res.string.collections),
                        items = buildList {
                            if (favCount > 0) add(
                                AppTileItem(
                                    title = stringResource(Res.string.favorites),
                                    subtitle = stringResource(Res.string.n_duas, favCount),
                                    leadingIcon = Lucide.Heart,
                                    leadingColor = c.primary,
                                    onClick = { onOpen(ReaderTarget.Favorites) },
                                ),
                            )
                            DuaSection.entries.forEach { section ->
                                add(
                                    AppTileItem(
                                        title = stringResource(section.labelRes),
                                        subtitle = stringResource(Res.string.n_duas, CATALOG[section]?.size ?: 0),
                                        leadingIcon = section.icon,
                                        onClick = { onOpen(ReaderTarget.Section(section)) },
                                    ),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

/** The hero: the day's supplication, set large and reverent on a filled panel. Tap to open it full. */
@Composable
private fun DuaOfTheDayCard(dua: Dua, onClick: () -> Unit) {
    val c = AppTheme.colors
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(c.primary).clickable(onClick = onClick).padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Lucide.Sparkles, null, tint = c.onPrimary.copy(alpha = 0.85f), modifier = Modifier.size(16.dp))
            Text(
                stringResource(Res.string.dua_of_the_day).uppercase(),
                fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
                color = c.onPrimary.copy(alpha = 0.85f),
            )
        }
        Spacer(Modifier.height(14.dp))
        ArabicText(dua.arabic, color = c.onPrimary, size = 24.sp)
        Spacer(Modifier.height(12.dp))
        Text(dua.translation, fontSize = 14.sp, color = c.onPrimary.copy(alpha = 0.92f), lineHeight = 21.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuaReaderScreen(
    title: String,
    duas: List<Dua>,
    onStartTasbih: (List<Dua>) -> Unit,
    onAddCustom: ((Dua) -> Unit)?, // null = no "add your own" (favorites / featured)
    onBack: () -> Unit,
) {
    val c = AppTheme.colors
    var showAdd by remember { mutableStateOf(false) }
    // the session: how many reps done per dua (this reader instance only). done = count >= repeat.
    val count = remember { mutableStateMapOf<String, Int>() }
    val doneCount = duas.count { (count[it.id] ?: 0) >= it.repeat }

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SessionProgress(
                    done = doneCount,
                    total = duas.size,
                    // whole set on beads stays available — an option, never the only path
                    onBeadsAll = if (duas.size > 1) ({ onStartTasbih(duas) }) else null,
                    onReset = { count.clear() },
                )
            }
            items(duas, key = { it.id }) { dua ->
                DuaCard(dua, count[dua.id] ?: 0, onAdvance = { advance(count, dua) }, onBeads = { onStartTasbih(listOf(dua)) })
            }
            if (onAddCustom != null) item { AddYourOwnTile { showAdd = true } }
        }
    }

    if (showAdd && onAddCustom != null) {
        AddDuaSheet(onSave = { onAddCustom(it); showAdd = false }, onDismiss = { showAdd = false })
    }
}

/** Advance a dua's session count: ×1 toggles like a checkbox; ×N taps up to the target, then it's done. */
private fun advance(count: MutableMap<String, Int>, dua: Dua) {
    val cur = count[dua.id] ?: 0
    count[dua.id] = if (dua.repeat <= 1) (if (cur >= 1) 0 else 1) else minOf(dua.repeat, cur + 1)
}

/** Session header: an animated bar with the running tally, plus optional whole-set beads and a reset. */
@Composable
private fun SessionProgress(done: Int, total: Int, onBeadsAll: (() -> Unit)?, onReset: () -> Unit) {
    val c = AppTheme.colors
    val allDone = total > 0 && done == total
    val pct by animateFloatAsState(if (total == 0) 0f else done.toFloat() / total, tween(400), label = "progress")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.cardColor).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            if (allDone) {
                Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.completed), color = c.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            } else {
                Text("$done / $total", color = c.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(Modifier.weight(1f))
            if (onBeadsAll != null) {
                Box(Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onBeadsAll), contentAlignment = Alignment.Center) {
                    Icon(Lucide.MoonStar, stringResource(Res.string.start_on_tasbih), tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
            Box(Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onReset), contentAlignment = Alignment.Center) {
                Icon(Lucide.RotateCcw, stringResource(Res.string.reset), tint = c.onSurfaceVariant, modifier = Modifier.size(17.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(c.primary.copy(alpha = 0.14f))) {
            Box(Modifier.fillMaxWidth(pct.coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(4.dp)).background(c.primary))
        }
    }
}

/** The completion tap-target: empty ring (unchecked) → filling arc (×N) → filled check (done). */
@Composable
private fun CountRing(count: Int, target: Int, done: Boolean, onTap: () -> Unit) {
    val c = AppTheme.colors
    val primary = c.primary
    val track = c.primary.copy(alpha = 0.16f)
    val frac = if (target <= 0) 0f else count.coerceAtMost(target).toFloat() / target
    val sweep by animateFloatAsState(360f * frac, tween(300), label = "sweep")
    val pop by animateFloatAsState(if (done) 1.06f else 1f, tween(180), label = "pop")
    Box(
        Modifier.size(52.dp).scale(pop).clip(CircleShape).background(if (done) primary else Color.Transparent).clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        if (!done) {
            Canvas(Modifier.fillMaxSize().padding(3.dp)) {
                val s = 4.dp.toPx()
                drawArc(track, -90f, 360f, false, style = Stroke(s, cap = StrokeCap.Round))
                if (sweep > 0f) drawArc(primary, -90f, sweep, false, style = Stroke(s, cap = StrokeCap.Round))
            }
        }
        when {
            done -> Icon(Lucide.Check, null, tint = c.onPrimary, modifier = Modifier.size(22.dp))
            target > 1 -> Text("$count", color = c.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            else -> Unit // ×1 unchecked = an empty ring
        }
    }
}

@Composable
private fun AddYourOwnTile(onClick: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.cardColor).clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Lucide.Plus, null, tint = c.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(stringResource(Res.string.add_your_own), color = c.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

/** Add a custom dua to this section: Arabic (required) + meaning + how many times to repeat. */
@Composable
private fun AddDuaSheet(onSave: (Dua) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    val reference = stringResource(Res.string.added_by_you)
    var arabic by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var count by remember { mutableStateOf(1) }

    AppBottomSheet(
        onDismiss = onDismiss,
        footer = {
            AppButton(
                stringResource(Res.string.save),
                onClick = { if (arabic.isNotBlank()) onSave(Dua(DuaStore.nextId(), arabic.trim(), "", meaning.trim(), reference, count)) },
                enabled = arabic.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {
        Text(stringResource(Res.string.add_your_own), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
        Spacer(Modifier.height(14.dp))
        AppTextField(arabic, { arabic = it }, placeholder = stringResource(Res.string.arabic_text))
        Spacer(Modifier.height(8.dp))
        AppTextField(meaning, { meaning = it }, placeholder = stringResource(Res.string.translation_optional))
        Spacer(Modifier.height(16.dp))
        Text(stringResource(Res.string.repetitions), fontSize = 13.sp, color = c.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1, 3, 7, 33, 100).forEach { n -> CountChip(n, n == count) { count = n } }
        }
        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun CountChip(n: Int, selected: Boolean, onClick: () -> Unit) {
    val c = AppTheme.colors
    Box(
        Modifier.clip(RoundedCornerShape(10.dp)).background(if (selected) c.primary else c.cardColor).clickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 10.dp),
    ) { Text("×$n", color = if (selected) c.onPrimary else c.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
}

/**
 * The signature — one supplication read top to bottom, then completed in place:
 * reference + favorite, Arabic, transliteration, meaning, then (×N target · optional beads) and the ring.
 * Done cards tint green and the ring becomes a check. [onAdvance] checks ×1 / counts ×N; [onBeads] hands to Tasbih.
 */
@Composable
private fun DuaCard(dua: Dua, progress: Int, onAdvance: () -> Unit, onBeads: () -> Unit) {
    val c = AppTheme.colors
    val target = dua.repeat
    val done = progress >= target
    val fav = dua.id in DuaStore.favIds
    val bg by animateColorAsState(if (done) c.primary.copy(alpha = 0.07f) else c.cardColor, tween(300), label = "cardbg")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(bg).padding(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                dua.reference.uppercase(),
                fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, color = c.primary,
                modifier = Modifier.weight(1f),
            )
            Box(
                Modifier.size(34.dp).clip(CircleShape).clickable { if (fav) DuaStore.favIds.remove(dua.id) else DuaStore.favIds.add(dua.id) },
                contentAlignment = Alignment.Center,
            ) { Icon(Lucide.Heart, null, tint = if (fav) c.primary else c.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
        }
        Spacer(Modifier.height(10.dp))
        ArabicText(dua.arabic, color = c.onSurface, size = 23.sp)
        if (dua.transliteration.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(dua.transliteration, fontSize = 13.sp, fontStyle = FontStyle.Italic, color = c.onSurfaceVariant, lineHeight = 20.sp)
        }
        if (dua.translation.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(dua.translation, fontSize = 14.sp, color = c.onSurface, lineHeight = 21.sp)
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = c.onSurfaceVariant.copy(alpha = 0.12f))
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                if (target > 1) {
                    Text("×$target", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    // optional: hand this repetitive dhikr to the immersive bead counter
                    Row(
                        Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onBeads).padding(vertical = 2.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Lucide.MoonStar, null, tint = c.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(stringResource(Res.string.use_beads), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = c.primary)
                    }
                }
            }
            CountRing(progress, target, done, onAdvance)
        }
    }
}

/** Arabic set right-to-left with a roomy line height, regardless of the UI language. */
@Composable
private fun ArabicText(text: String, color: Color, size: TextUnit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Text(
            text,
            modifier = Modifier.fillMaxWidth(),
            fontSize = size,
            fontWeight = FontWeight.Medium,
            lineHeight = size * 1.9f,
            textAlign = TextAlign.Start, // Start under forced-RTL = right-aligned
            color = color,
        )
    }
}
