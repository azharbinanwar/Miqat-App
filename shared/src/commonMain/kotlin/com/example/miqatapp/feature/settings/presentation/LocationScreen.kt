package com.example.miqatapp.feature.settings.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronLeft
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.Navigation
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.X
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.enums.countryLabel
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.prefs.Prefs
import com.example.miqatapp.core.widgets.AppTextField
import com.example.miqatapp.core.widgets.AppTile
import com.example.miqatapp.core.widgets.AppTileGroup
import com.example.miqatapp.core.widgets.AppTileItem
import com.example.miqatapp.core.widgets.StateView
import com.example.miqatapp.core.widgets.TilePosition
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.location
import com.example.miqatapp.resources.no_cities_found
import com.example.miqatapp.resources.saved
import com.example.miqatapp.resources.search_city
import com.example.miqatapp.resources.try_a_different_search
import com.example.miqatapp.resources.use_current_location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

/** A place prayer times are computed for. Carries [lat]/[lng]/[timezone] — the data the calc engine needs. */
private data class City(val name: String, val ascii: String, val lat: Double, val lng: Double, val country: String, val timezone: String)

/** Built-in default (Kaaba) — used until the user picks/detects their own city; also the permission-denied fallback. */
private val MAKKAH = City("Makkah", "Makkah", 21.4225, 39.8262, "SA", "Asia/Riyadh")

/** Parse the bundled GeoNames-style TSV: name, asciiname, lat, lng, countryCode, timezone. */
private fun parseCities(bytes: ByteArray): List<City> =
    bytes.decodeToString().lineSequence().mapNotNull { line ->
        if (line.isBlank()) return@mapNotNull null
        val p = line.split('\t')
        if (p.size < 6) return@mapNotNull null
        val lat = p[2].toDoubleOrNull() ?: return@mapNotNull null
        val lng = p[3].toDoubleOrNull() ?: return@mapNotNull null
        City(p[0], p[1], lat, lng, p[4], p[5])
    }.toList()

/**
 * Location picker — a tidy hub: "Use current location" + "Search for a city" (opens a full-screen search
 * over the bundled offline catalog, ~49k cities), then your saved cities with the active one checked.
 * Picking a city saves + activates it. ponytail: mock GPS + state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    val saved = remember { mutableStateListOf(MAKKAH) }
    var active by remember { mutableStateOf(MAKKAH) }
    var showSearch by remember { mutableStateOf(false) }

    // load the 49k-row catalog once, off the main thread — so opening search is instant
    var all by remember { mutableStateOf<List<City>>(emptyList()) }
    LaunchedEffect(Unit) { all = withContext(Dispatchers.Default) { parseCities(Res.readBytes("files/cities.txt")) } }

    fun choose(city: City) {
        if (saved.none { it.name == city.name && it.country == city.country }) saved.add(0, city)
        active = city
        Prefs.activeCity = city.name // surfaced in the drawer header
    }

    // full-screen search takes over when open (LazyColumn handles hundreds of rows efficiently)
    if (showSearch) {
        CitySearchScreen(all = all, onPick = { choose(it); showSearch = false }, onClose = { showSearch = false })
        return
    }

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.location), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp).verticalScroll(rememberScrollState())) {
            AppTileGroup(
                items = listOf(
                    // ponytail: mock GPS — real fix (permissions + on-device coords) lands with the calc engine
                    AppTileItem(title = stringResource(Res.string.use_current_location), leadingIcon = Lucide.Navigation, onClick = { choose(MAKKAH) }),
                    AppTileItem(title = stringResource(Res.string.search_city), leadingIcon = Lucide.Search, onClick = { showSearch = true }),
                ),
            )
            Spacer(Modifier.height(12.dp))
            AppTileGroup(
                title = stringResource(Res.string.saved),
                items = saved.map { city ->
                    val isActive = city.name == active.name && city.country == active.country
                    AppTileItem(
                        title = city.name,
                        subtitle = countryLabel(city.country),
                        leadingIcon = Lucide.MapPin,
                        selected = isActive,
                        trailing = {
                            if (isActive) Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(20.dp))
                            else Icon(Lucide.X, null, tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp).clickable { saved.remove(city) })
                        },
                        onClick = { choose(city) },
                    )
                },
            )
        }
    }
}

/**
 * Full-screen city search over the whole offline catalog. Pinned search bar on top, a lazy list of up to 200
 * matches below (prefix matches ranked first). All state is local — it's gone when the screen closes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CitySearchScreen(all: List<City>, onPick: (City) -> Unit, onClose: () -> Unit) {
    val c = AppTheme.colors
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<City>>(emptyList()) }
    LaunchedEffect(query, all) {
        val q = query.trim()
        results = if (q.isBlank()) emptyList() else withContext(Dispatchers.Default) {
            all.asSequence()
                .filter { it.ascii.contains(q, ignoreCase = true) || it.name.contains(q, ignoreCase = true) }
                .sortedWith(compareByDescending<City> { it.ascii.startsWith(q, ignoreCase = true) }.thenBy { it.ascii })
                .take(200).toList()
        }
    }

    Scaffold(
        containerColor = c.scaffoldBackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.search_city), fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onClose) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.scaffoldBackgroundColor,
                    titleContentColor = c.onSurface,
                    navigationIconContentColor = c.onSurface,
                ),
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            AppTextField(value = query, onValueChange = { query = it }, placeholder = stringResource(Res.string.search_city))
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when {
                    all.isEmpty() -> CircularProgressIndicator(color = c.primary, modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp))
                    query.isBlank() -> Unit
                    results.isEmpty() -> StateView(
                        title = stringResource(Res.string.no_cities_found),
                        message = stringResource(Res.string.try_a_different_search),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                    )
                    else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        itemsIndexed(results, key = { _, city -> city.name + city.country + city.lat }) { i, city ->
                            AppTile(
                                title = city.name,
                                subtitle = countryLabel(city.country),
                                leadingIcon = Lucide.MapPin,
                                position = when {
                                    results.size == 1 -> TilePosition.Single
                                    i == 0 -> TilePosition.First
                                    i == results.lastIndex -> TilePosition.Last
                                    else -> TilePosition.Middle
                                },
                                onClick = { onPick(city) },
                            )
                        }
                    }
                }
            }
        }
    }
}
