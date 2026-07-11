package com.example.miqatapp.feature.tasbih.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.Layers
import com.composables.icons.lucide.History
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Menu
import com.composables.icons.lucide.Minus
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.navigation.AppRoute
import com.example.miqatapp.core.navigation.LocalNavController
import com.example.miqatapp.core.components.AppBottomSheet
import com.example.miqatapp.core.components.AppButton
import com.example.miqatapp.core.components.AppButtonVariant
import com.example.miqatapp.core.components.AppTextField
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileGroupReorderable
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.heart_filled
import com.example.miqatapp.resources.heart_outline
import com.example.miqatapp.resources.tasbih
import com.example.miqatapp.resources.tasbih_add_more_azkar
import com.example.miqatapp.resources.tasbih_add_to_favorites
import com.example.miqatapp.resources.tasbih_add_your_own
import com.example.miqatapp.resources.tasbih_cancel
import com.example.miqatapp.resources.tasbih_cancel_selection
import com.example.miqatapp.resources.tasbih_cat_durood
import com.example.miqatapp.resources.tasbih_cat_names
import com.example.miqatapp.resources.tasbih_cat_quranic
import com.example.miqatapp.resources.tasbih_cat_tasbihat
import com.example.miqatapp.resources.tasbih_checked
import com.example.miqatapp.resources.tasbih_create_start
import com.example.miqatapp.resources.tasbih_custom
import com.example.miqatapp.resources.tasbih_favorited
import com.example.miqatapp.resources.tasbih_favorites
import com.example.miqatapp.resources.tasbih_history
import com.example.miqatapp.resources.tasbih_menu
import com.example.miqatapp.resources.tasbih_my_sets
import com.example.miqatapp.resources.tasbih_n_azkar
import com.example.miqatapp.resources.tasbih_n_selected
import com.example.miqatapp.resources.tasbih_new_set
import com.example.miqatapp.resources.tasbih_remove
import com.example.miqatapp.resources.tasbih_repetitions
import com.example.miqatapp.resources.tasbih_resume
import com.example.miqatapp.resources.tasbih_see_all
import com.example.miqatapp.resources.tasbih_set_custom_count
import com.example.miqatapp.resources.tasbih_set_name
import com.example.miqatapp.resources.tasbih_show_less
import com.example.miqatapp.resources.tasbih_subtitle_optional
import com.example.miqatapp.resources.tasbih_view_set
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// ───────────────────────── mock catalog (ponytail: replace with DB later) ─────────────────────────

enum class ZikrCategory(val label: String) {
    Tasbihat("Tasbihat"),
    Durood("Durood & Salawat"),
    Names("Names of Allah"),
    Quranic("Qur'anic"),
}

/** A single dhikr. [defaultCount] 0 = unlimited. */
data class Zikr(val id: String, val title: String, val arabic: String, val defaultCount: Int, val category: ZikrCategory, val favorite: Boolean = false)

private val CATALOG = listOf(
    Zikr("subhanallah", "SubhanAllah", "سُبْحَانَ ٱللَّٰه", 33, ZikrCategory.Tasbihat, favorite = true),
    Zikr("alhamdulillah", "Alhamdulillah", "ٱلْحَمْدُ لِلَّٰه", 33, ZikrCategory.Tasbihat),
    Zikr("allahuakbar", "Allahu Akbar", "ٱللَّٰهُ أَكْبَر", 34, ZikrCategory.Tasbihat),
    Zikr("tahlil", "La ilaha illAllah", "لَا إِلَٰهَ إِلَّا ٱللَّٰه", 100, ZikrCategory.Tasbihat, favorite = true),
    Zikr("istighfar", "Astaghfirullah", "أَسْتَغْفِرُ ٱللَّٰه", 100, ZikrCategory.Tasbihat, favorite = true),
    Zikr("durood-ibrahimi", "Durood-e-Ibrahimi", "ٱللَّٰهُمَّ صَلِّ عَلَىٰ مُحَمَّد", 100, ZikrCategory.Durood, favorite = true),
    Zikr("durood-pak", "Durood-e-Pak", "صَلَّى ٱللَّٰهُ عَلَيْهِ وَسَلَّم", 100, ZikrCategory.Durood),
    Zikr("ya-rahman", "Ya Rahman", "يَا رَحْمَٰن", 100, ZikrCategory.Names),
    Zikr("ya-raheem", "Ya Raheem", "يَا رَحِيم", 100, ZikrCategory.Names),
    Zikr("ya-wadud", "Ya Wadud", "يَا وَدُود", 100, ZikrCategory.Names),
    Zikr("tasbih-hamd", "SubhanAllahi wa bihamdihi", "سُبْحَانَ ٱللَّٰهِ وَبِحَمْدِه", 100, ZikrCategory.Quranic),
    Zikr("hawqala", "La hawla wa la quwwata", "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِٱللَّٰه", 100, ZikrCategory.Quranic),
)

private fun zikrById(id: String) = CATALOG.first { it.id == id }
private val COUNT_PRESETS = listOf(33, 99, 100, 0) // 0 = unlimited (∞)

/** Hand-off to the counter. ponytail: in-memory; swap for VM/DB-backed session. */
object TasbihRun {
    var queue: List<Zikr> = emptyList()
}

/** A saved/named group of azkar. */
data class TasbihSet(val id: String, val name: String, val subtitle: String, val items: List<Zikr>, val favorite: Boolean)

/** Saved sets + favorited single azkar. ponytail: in-memory; move to DB later. */
object TasbihStore {
    val sets = mutableStateListOf<TasbihSet>()
    val favIds = mutableStateListOf<String>()
    private var seq = 0
    private var seeded = false
    fun ensureSeeded() {
        if (seeded) return
        favIds.addAll(CATALOG.filter { it.favorite }.map { it.id })
        // a sample favourited set so the design is visible up front
        sets.add(TasbihSet(nextId(), "Morning Adhkar", "After Fajr", listOf(zikrById("subhanallah"), zikrById("alhamdulillah"), zikrById("allahuakbar")), favorite = true))
        seeded = true
    }
    fun nextId() = "set-${seq++}"
}

// ───────────────────────────────────────── screen ─────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbihHubScreen(onHistory: () -> Unit = {}) {
    val c = AppTheme.colors
    val drawerState = LocalDrawerState.current
    val nav = LocalNavController.current
    val scope = rememberCoroutineScope()
    remember { TasbihStore.ensureSeeded() }

    // selection tracks ORDER (for reordering) + per-id count
    val order = remember { mutableStateListOf<String>() }
    val counts = remember { mutableStateMapOf<String, Int>() }
    var countSheetFor by remember { mutableStateOf<Zikr?>(null) }

    // create-set sheet state
    var sheetVisible by remember { mutableStateOf(false) }
    var setName by remember { mutableStateOf("") }
    var setSubtitle by remember { mutableStateOf("") }
    var setFav by remember { mutableStateOf(false) }
    var favExpanded by remember { mutableStateOf(false) }

    fun toggle(z: Zikr) { if (z.id in order) { order.remove(z.id); counts.remove(z.id) } else { order.add(z.id); counts[z.id] = z.defaultCount } }
    // per-run count override rides inside the zikr itself (a copy — the catalog original is untouched)
    fun selectedItems() = order.map { id -> zikrById(id).run { copy(defaultCount = counts[id] ?: defaultCount) } }
    fun start(items: List<Zikr>) { if (items.isNotEmpty()) { TasbihRun.queue = items; nav.navigate(AppRoute.TasbihCounter) } }
    fun clearSelection() { order.clear(); counts.clear(); sheetVisible = false; setName = ""; setSubtitle = ""; setFav = false }
    val selectionMode = order.isNotEmpty() // long-press a tile to enter; tiles then multi-select

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (selectionMode) stringResource(Res.string.tasbih_n_selected, order.size) else stringResource(Res.string.tasbih)) },
                navigationIcon = {
                    if (selectionMode) IconButton(onClick = { clearSelection() }) { Icon(Lucide.X, stringResource(Res.string.tasbih_cancel_selection)) }
                    else IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Lucide.Menu, stringResource(Res.string.tasbih_menu)) }
                },
                actions = {
                    if (!selectionMode) IconButton(onClick = onHistory) { Icon(Lucide.History, stringResource(Res.string.tasbih_history)) }
                },
            )
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item {
                    ActiveSessionCard(name = "SubhanAllah", count = 18, target = 33) { start(listOf(zikrById("subhanallah"))) }
                }
                item { Spacer(Modifier.height(8.dp)) }

                // Favorites = starred single azkar + starred sets, with a "See all" once it grows.
                val favSingles = TasbihStore.favIds.toList().map { zikrById(it) }
                val favSets = TasbihStore.sets.filter { it.favorite }
                if (favSingles.isNotEmpty() || favSets.isNotEmpty()) {
                    item {
                        val all = buildList {
                            favSingles.forEach { z ->
                                // a favourite single azkar is selectable for a set too — by id, so it also shows selected in its category
                                add(
                                    AppTileItem(
                                        title = z.arabic, subtitle = z.title,
                                        selected = z.id in order,
                                        onClick = { if (selectionMode) toggle(z) else start(listOf(z.copy(defaultCount = counts[z.id] ?: z.defaultCount))) },
                                        onLongClick = { toggle(z) },
                                        trailing = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                CountTag(counts[z.id] ?: z.defaultCount) { countSheetFor = z }
                                                Spacer(Modifier.width(4.dp))
                                                if (selectionMode) {
                                                    SelectCheck(z.id in order) { toggle(z) }
                                                } else {
                                                    HeartIcon(filled = true, tint = c.primary, size = 20.dp, modifier = Modifier.clickable { TasbihStore.favIds.remove(z.id) })
                                                }
                                            }
                                        },
                                    ),
                                )
                            }
                            favSets.forEach { s ->
                                add(
                                    AppTileItem(
                                        title = s.name, subtitle = stringResource(Res.string.tasbih_n_azkar, s.items.size), leadingIcon = Lucide.Layers,
                                        onClick = { if (!selectionMode) start(s.items) }, // a set can't be nested into a set
                                        trailing = { HeartIcon(filled = true, tint = c.primary, size = 20.dp, modifier = Modifier.clickable { val i = TasbihStore.sets.indexOf(s); if (i >= 0) TasbihStore.sets[i] = s.copy(favorite = false) }) },
                                    ),
                                )
                            }
                        }
                        val shown = if (favExpanded) all else all.take(3)
                        val favItems = if (all.size > 3) {
                            shown + AppTileItem(
                                title = if (favExpanded) stringResource(Res.string.tasbih_show_less) else stringResource(Res.string.tasbih_see_all, all.size),
                                leadingIcon = if (favExpanded) Lucide.ChevronUp else Lucide.ChevronDown,
                                onClick = { favExpanded = !favExpanded },
                            )
                        } else {
                            shown
                        }
                        AppTileGroup(title = stringResource(Res.string.tasbih_favorites), items = favItems)
                    }
                }

                if (TasbihStore.sets.isNotEmpty()) {
                    item {
                        AppTileGroup(
                            title = stringResource(Res.string.tasbih_my_sets),
                            items = TasbihStore.sets.map { s ->
                                AppTileItem(
                                    title = s.name,
                                    subtitle = if (s.subtitle.isNotBlank()) s.subtitle else stringResource(Res.string.tasbih_n_azkar, s.items.size),
                                    leadingIcon = Lucide.Layers,
                                    badge = if (s.favorite) ({ HeartIcon(filled = true, tint = c.primary, size = 14.dp) }) else null,
                                    onClick = { start(s.items) },
                                )
                            },
                        )
                    }
                }

                ZikrCategory.entries.forEach { cat ->
                    val rows = CATALOG.filter { it.category == cat }
                    item {
                        AppTileGroup(
                            title = categoryLabel(cat),
                            items = rows.map { z ->
                                AppTileItem(
                                    title = z.arabic,
                                    subtitle = z.title,
                                    selected = z.id in order,
                                    onClick = { if (selectionMode) toggle(z) else start(listOf(z.copy(defaultCount = counts[z.id] ?: z.defaultCount))) },
                                    onLongClick = { toggle(z) }, // long-press enters selection mode
                                    trailing = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CountTag(counts[z.id] ?: z.defaultCount) { countSheetFor = z }
                                            Spacer(Modifier.width(4.dp))
                                            if (selectionMode) {
                                                SelectCheck(z.id in order) { toggle(z) }
                                            } else {
                                                val fav = z.id in TasbihStore.favIds
                                                HeartIcon(filled = fav, tint = if (fav) c.primary else c.onSurfaceVariant, size = 22.dp, modifier = Modifier.clickable { if (fav) TasbihStore.favIds.remove(z.id) else TasbihStore.favIds.add(z.id) })
                                            }
                                        }
                                    },
                                )
                            } + AppTileItem(title = stringResource(Res.string.tasbih_add_your_own), leadingIcon = Lucide.Plus, onClick = { /* ponytail: add-your-own sheet later */ }),
                        )
                    }
                }
            }

            if (order.isNotEmpty()) {
                AppButton(
                    text = stringResource(Res.string.tasbih_view_set, order.size),
                    onClick = { sheetVisible = true },
                    modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                )
            }
        }
    }

    if (sheetVisible) {
        ViewSetSheet(
            order = order, counts = counts,
            name = setName, onName = { setName = it },
            subtitle = setSubtitle, onSubtitle = { setSubtitle = it },
            favorite = setFav, onToggleFav = { setFav = !setFav },
            onEditCount = { countSheetFor = it },
            onRemove = { id -> order.remove(id); counts.remove(id) },
            onAddMore = { sheetVisible = false },        // keep selection, go back to list
            onCancel = { clearSelection() },             // discard
            onCreate = {
                val items = selectedItems()
                val name = setName.ifBlank { items.first().title + if (items.size > 1) " +${items.size - 1}" else "" }
                if (setFav || setName.isNotBlank()) TasbihStore.sets.add(0, TasbihSet(TasbihStore.nextId(), name, setSubtitle, items, setFav))
                clearSelection()
                start(items)
            },
        )
    }

    countSheetFor?.let { z ->
        CountSheet(
            current = counts[z.id] ?: z.defaultCount,
            onPick = { counts[z.id] = it }, // just set the count — selecting is the check's job
            onDismiss = { countSheetFor = null },
        )
    }
}

@Composable
private fun ActiveSessionCard(name: String, count: Int, target: Int, onResume: () -> Unit) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(c.primary).clickable(onClick = onResume).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(Res.string.tasbih_resume), fontSize = 12.sp, color = c.onPrimary.copy(alpha = 0.8f))
            Spacer(Modifier.height(2.dp))
            Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = c.onPrimary)
            Text("$count / $target", fontSize = 13.sp, color = c.onPrimary.copy(alpha = 0.9f))
        }
        Box(Modifier.size(40.dp).clip(CircleShape).background(c.onPrimary.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
            Icon(Lucide.Play, stringResource(Res.string.tasbih_resume), tint = c.onPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

/** Repetition count shown on every catalog tile — tap to edit before starting. */
@Composable
internal fun CountTag(shownCount: Int, onEditCount: () -> Unit) {
    val c = AppTheme.colors
    Text(
        if (shownCount == 0) "∞" else "×$shownCount",
        fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.onSurfaceVariant,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable(onClick = onEditCount).padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

/** Selection-mode trailing: a check circle (tap = add/remove). */
@Composable
internal fun SelectCheck(selected: Boolean, onToggle: () -> Unit) {
    val c = AppTheme.colors
    Box(
        Modifier.size(26.dp).clip(CircleShape).background(if (selected) c.primary else Color.Transparent)
            .border(1.5.dp, if (selected) c.primary else c.onSurfaceVariant.copy(alpha = 0.4f), CircleShape).clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) { if (selected) Icon(Lucide.Check, stringResource(Res.string.tasbih_checked), tint = c.onPrimary, modifier = Modifier.size(16.dp)) }
}

/** View/edit the selected set before creating: name, subtitle, ❤ favorite, items (reorder/count/remove), Add more, then Cancel/Create. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewSetSheet(
    order: MutableList<String>,
    counts: Map<String, Int>,
    name: String, onName: (String) -> Unit,
    subtitle: String, onSubtitle: (String) -> Unit,
    favorite: Boolean, onToggleFav: () -> Unit,
    onEditCount: (Zikr) -> Unit,
    onRemove: (String) -> Unit,
    onAddMore: () -> Unit,
    onCancel: () -> Unit,
    onCreate: () -> Unit,
) {
    val c = AppTheme.colors
    AppBottomSheet(
        onDismiss = onAddMore,
        footer = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppButton(stringResource(Res.string.tasbih_cancel), onClick = onCancel, variant = AppButtonVariant.Outline, modifier = Modifier.weight(1f))
                AppButton(stringResource(Res.string.tasbih_create_start), onClick = onCreate, modifier = Modifier.weight(1f))
            }
        },
    ) {
        Text(stringResource(Res.string.tasbih_new_set), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = c.onSurface)
        Spacer(Modifier.height(12.dp))
        AppTextField(name, onName, placeholder = stringResource(Res.string.tasbih_set_name))
        Spacer(Modifier.height(8.dp))
        AppTextField(subtitle, onSubtitle, placeholder = stringResource(Res.string.tasbih_subtitle_optional))
        Spacer(Modifier.height(10.dp))
        AppTile(
            title = if (favorite) stringResource(Res.string.tasbih_favorited) else stringResource(Res.string.tasbih_add_to_favorites),
            onClick = onToggleFav,
            trailing = { HeartIcon(filled = favorite, tint = if (favorite) c.primary else c.onSurfaceVariant) },
        )
        Spacer(Modifier.height(12.dp))
        AppTileGroupReorderable(
            items = order.toList().map { id ->
                val z = zikrById(id)
                AppTileItem(
                    title = z.title,
                    subtitle = z.arabic,
                    trailing = { SetRowTrailing(counts[id] ?: z.defaultCount, onEditCount = { onEditCount(z) }, onRemove = { onRemove(id) }) },
                )
            },
            onReorder = { from, to -> order.add(to, order.removeAt(from)) }, // long-press a row, drag to reorder
        )
        Spacer(Modifier.height(4.dp))
        AppTile(title = stringResource(Res.string.tasbih_add_more_azkar), leadingIcon = Lucide.Plus, onClick = onAddMore)
    }
}

/** Favorite heart from drawable files (Lucide has no heart): filled or outline, tintable. */
@Composable
internal fun HeartIcon(filled: Boolean, tint: Color, size: Dp = 22.dp, modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(if (filled) Res.drawable.heart_filled else Res.drawable.  heart_outline),
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(size),
    )
}

@Composable
private fun SetRowTrailing(count: Int, onEditCount: () -> Unit, onRemove: () -> Unit) {
    val c = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (count == 0) "∞" else "×$count",
            color = c.primary, fontWeight = FontWeight.Bold,
            modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick = onEditCount).padding(horizontal = 8.dp, vertical = 4.dp),
        )
        IconButton(onClick = onRemove, modifier = Modifier.size(30.dp)) {
            Icon(Lucide.Minus, stringResource(Res.string.tasbih_remove), tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}

/** Bottom sheet to pick repetitions: presets + ±1/±10/±100 quick-adjust. */
@Composable
internal fun CountSheet(current: Int, onPick: (Int) -> Unit, onDismiss: () -> Unit) {
    val c = AppTheme.colors
    AppBottomSheet(onDismiss = onDismiss) {
        Text(stringResource(Res.string.tasbih_repetitions), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = c.onSurface)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            COUNT_PRESETS.forEach { p ->
                val isSel = p == current
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(if (isSel) c.primary else c.cardColor).clickable { onPick(p); onDismiss() }.padding(horizontal = 18.dp, vertical = 12.dp),
                ) { Text(if (p == 0) "∞" else "$p", fontWeight = FontWeight.Bold, color = if (isSel) c.onPrimary else c.onSurface) }
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(stringResource(Res.string.tasbih_custom), fontSize = 13.sp, color = c.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        var custom by remember { mutableStateOf(if (current > 0) current else 33) }
        Text("$custom", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = c.onSurface, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(-100, -10, -1, 1, 10, 100).forEach { d ->
                AdjChip(if (d > 0) "+$d" else "$d", Modifier.weight(1f)) { custom = (custom + d).coerceAtLeast(1) }
            }
        }
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(c.primary).clickable { onPick(custom); onDismiss() }.padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) { Text(stringResource(Res.string.tasbih_set_custom_count), fontWeight = FontWeight.Bold, color = c.onPrimary) }
        Spacer(Modifier.height(4.dp))
    }
}

// Localized category title — rendered here only; ZikrCategory's `label` stays as a stable id.
@Composable
private fun categoryLabel(category: ZikrCategory): String = stringResource(
    when (category) {
        ZikrCategory.Tasbihat -> Res.string.tasbih_cat_tasbihat
        ZikrCategory.Durood -> Res.string.tasbih_cat_durood
        ZikrCategory.Names -> Res.string.tasbih_cat_names
        ZikrCategory.Quranic -> Res.string.tasbih_cat_quranic
    },
)

@Composable
private fun AdjChip(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = AppTheme.colors
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(c.cardColor).clickable(onClick = onClick).padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = c.onSurface)
    }
}
