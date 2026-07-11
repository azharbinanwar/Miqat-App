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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.core.constants.countryLabel
import com.example.miqatapp.core.locale.tr
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.core.location.nearestTo
import com.example.miqatapp.core.constants.defaults.MiqatDefaults
import com.example.miqatapp.core.location.rememberGeoLocator
import com.example.miqatapp.core.permissions.AppPermission
import com.example.miqatapp.core.permissions.PermissionDeniedSheet
import com.example.miqatapp.core.permissions.PermissionStatus
import com.example.miqatapp.core.permissions.rememberPermissionService
import com.example.miqatapp.core.components.AppTextField
import com.example.miqatapp.core.components.AppTile
import com.example.miqatapp.core.components.AppTileGroup
import com.example.miqatapp.core.components.AppTileItem
import com.example.miqatapp.core.components.StateView
import com.example.miqatapp.core.components.TilePosition
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.back
import com.example.miqatapp.resources.clear_search
import com.example.miqatapp.resources.location
import com.example.miqatapp.resources.no_cities_found
import com.example.miqatapp.resources.search_city_hint_message
import com.example.miqatapp.resources.search_city_hint_title
import com.example.miqatapp.resources.saved
import com.example.miqatapp.resources.search_city
import com.example.miqatapp.resources.try_a_different_search
import com.example.miqatapp.resources.use_current_location
import androidx.compose.runtime.rememberCoroutineScope
import com.example.miqatapp.resources.location_permission_needed
import com.example.miqatapp.resources.location_permission_rationale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource

/** Same place regardless of coord precision — dedupe key for the saved list. */
private fun Place.sameAs(other: Place) = name == other.name && countryCode == other.countryCode

/**
 * Location picker — a tidy hub: "Use current location" + "Search for a city" (opens a full-screen search
 * over the bundled offline catalog, ~49k cities), then your saved cities with the active one checked.
 * Picking a city saves + activates it. ponytail: mock GPS + state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(onBack: () -> Unit = {}) {
    val c = AppTheme.colors
    // Single source of truth — the repo resolves Prefs ?: MiqatDefaults and emits on every change.
    val active by LocationStore.activePlace.collectAsState()
    val savedRaw by LocationStore.savedPlaces.collectAsState()
    val saved = savedRaw.ifEmpty { listOf(active) } // show the active place even before anything is saved
    var showSearch by remember { mutableStateOf(false) }

    // load the 49k-row catalog once, off the main thread — so opening search is instant
    var all by remember { mutableStateOf<List<Place>>(emptyList()) }
    LaunchedEffect(Unit) { all = withContext(Dispatchers.Default) { Place.fromCatalog(Res.readBytes("files/cities.txt")) } }

    // GPS: request permission → get a fix → snap to the nearest catalog city (offline) → save it.
    val perms = rememberPermissionService()
    val geo = rememberGeoLocator()
    val scope = rememberCoroutineScope()
    var showDeniedSheet by remember { mutableStateOf(false) }
    var locating by remember { mutableStateOf(false) } // GPS in flight — drives the tile spinner
    fun useCurrentLocation() {
        if (locating) return
        locating = true
        scope.launch {
            try {
                when (perms.request(AppPermission.Location)) {
                    PermissionStatus.Granted -> {
                        val fix = geo.current()
                        val place = fix?.let { all.nearestTo(it.latitude, it.longitude) }
                        if (place != null) LocationStore.setActive(place) // else: no fix / catalog still loading — keep current
                    }
                    else -> showDeniedSheet = true // denied/dismissed → explain + offer Settings
                }
            } finally {
                locating = false
            }
        }
    }

    // full-screen search takes over when open (LazyColumn handles hundreds of rows efficiently)
    if (showSearch) {
        CitySearchScreen(all = all, onPick = { LocationStore.setActive(it); showSearch = false }, onClose = { showSearch = false })
        return
    }

    if (showDeniedSheet) {
        PermissionDeniedSheet(
            title = stringResource(Res.string.location_permission_needed),
            message = stringResource(Res.string.location_permission_rationale),
            onOpenSettings = { showDeniedSheet = false; perms.openAppSettings() },
            onDismiss = { showDeniedSheet = false },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.location)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp).verticalScroll(rememberScrollState())) {
            AppTileGroup(
                items = listOf(
                    AppTileItem(
                        title = stringResource(Res.string.use_current_location),
                        leadingIcon = Lucide.Navigation,
                        trailing = { if (locating) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = c.primary, strokeWidth = 2.dp) },
                        onClick = { useCurrentLocation() },
                    ),
                    AppTileItem(title = stringResource(Res.string.search_city), leadingIcon = Lucide.Search, onClick = { showSearch = true }),
                ),
            )
            Spacer(Modifier.height(12.dp))
            AppTileGroup(
                title = stringResource(Res.string.saved),
                items = saved.map { place ->
                    val isActive = place.sameAs(active)
                    AppTileItem(
                        title = place.name,
                        subtitle = place.countryLabel,
                        leadingIcon = Lucide.MapPin,
                        selected = isActive,
                        trailing = {
                            if (isActive) Icon(Lucide.Check, null, tint = c.primary, modifier = Modifier.size(20.dp))
                            else Icon(Lucide.X, null, tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp).clickable { LocationStore.remove(place) })
                        },
                        onClick = { LocationStore.setActive(place) },
                    )
                },
            )
            Spacer(Modifier.height(12.dp))
            AppTileGroup(
                title = "Suggested",   // ponytail: to resources later
                items = MiqatDefaults.places.map { place ->
                    AppTileItem(
                        title = place.name,
                        subtitle = place.countryLabel,
                        leadingIcon = Lucide.MapPin,
                        selected = place.sameAs(active),
                        onClick = { LocationStore.setActive(place) },
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
private fun CitySearchScreen(all: List<Place>, onPick: (Place) -> Unit, onClose: () -> Unit) {
    val c = AppTheme.colors
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Place>>(emptyList()) }
    LaunchedEffect(query, all) {
        val q = query.trim()
        results = if (q.isBlank()) emptyList() else withContext(Dispatchers.Default) {
            all.asSequence()
                .filter { it.ascii.contains(q, ignoreCase = true) || it.name.contains(q, ignoreCase = true) }
                .sortedWith(compareByDescending<Place> { it.ascii.startsWith(q, ignoreCase = true) }.thenBy { it.ascii })
                .take(200).toList()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.search_city)) },
                navigationIcon = { IconButton(onClick = onClose) { Icon(tr(Lucide.ChevronLeft, Lucide.ChevronRight), stringResource(Res.string.back)) } },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            AppTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = stringResource(Res.string.search_city),
                trailing = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) { Icon(Lucide.X, stringResource(Res.string.clear_search), tint = c.onSurfaceVariant, modifier = Modifier.size(18.dp)) } }
                } else null,
            )
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when {
                    all.isEmpty() -> CircularProgressIndicator(color = c.primary, modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp))
                    query.isBlank() -> StateView(
                        title = stringResource(Res.string.search_city_hint_title),
                        message = stringResource(Res.string.search_city_hint_message),
                        icon = { Icon(Lucide.Search, null, tint = c.primary, modifier = Modifier.size(40.dp)) },
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                    )
                    results.isEmpty() -> StateView(
                        title = stringResource(Res.string.no_cities_found),
                        message = stringResource(Res.string.try_a_different_search),
                        icon = { Icon(Lucide.MapPin, null, tint = c.onSurfaceVariant, modifier = Modifier.size(40.dp)) },
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 24.dp),
                    )
                    else -> LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        itemsIndexed(results, key = { _, place -> place.name + place.countryCode + place.latitude }) { i, place ->
                            AppTile(
                                title = place.name,
                                subtitle = place.countryLabel,
                                leadingIcon = Lucide.MapPin,
                                position = when {
                                    results.size == 1 -> TilePosition.Single
                                    i == 0 -> TilePosition.First
                                    i == results.lastIndex -> TilePosition.Last
                                    else -> TilePosition.Middle
                                },
                                onClick = { onPick(place) },
                            )
                        }
                    }
                }
            }
        }
    }
}
