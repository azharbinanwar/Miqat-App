package com.example.miqatapp.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.miqatapp.core.components.LocalDrawerState
import com.example.miqatapp.core.constants.Place
import com.example.miqatapp.core.enums.CalculationMethod
import com.example.miqatapp.core.location.LocationMoveSheet
import com.example.miqatapp.core.location.LocationResolver
import com.example.miqatapp.core.location.rememberGeoLocator
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.feature.home.presentation.components.DailyVerseCard
import com.example.miqatapp.feature.home.presentation.components.MulkReminderCard
import com.example.miqatapp.feature.home.presentation.components.PrayerSceneHeader
import com.example.miqatapp.feature.home.presentation.components.SceneDebugOverlay
import com.example.miqatapp.feature.home.presentation.components.StreakCard
import com.example.miqatapp.feature.home.presentation.components.TodayPrayers
import com.example.miqatapp.feature.miqat.store.MiqatCalculationStore
import kotlinx.coroutines.launch

private val ExpandedHeader = 380.dp
private val CollapsedHeader = 116.dp

@Composable
fun HomeScreen() {
    val place by LocationStore.activePlace.collectAsState()
    val calc by MiqatCalculationStore.calculation.collectAsState()

    // silent GPS check — never prompts, only offers a move when you've actually travelled
    val geo = rememberGeoLocator()
    var moveCandidate by remember { mutableStateOf<Place?>(null) }
    LaunchedEffect(Unit) {
        val fix = geo.current() ?: return@LaunchedEffect
        moveCandidate = LocationResolver.detectMove(LocationStore.activePlace.value, fix)
    }

    val scroll = rememberScrollState()
    val density = LocalDensity.current
    val rangePx = with(density) { (ExpandedHeader - CollapsedHeader).toPx() }
    val fraction = (scroll.value / rangePx).coerceIn(0f, 1f)
    val drawerState = LocalDrawerState.current
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) {
            Spacer(Modifier.height(ExpandedHeader))
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                StreakCard()
                TodayPrayers()
                MulkReminderCard()
                DailyVerseCard()
                Spacer(Modifier.height(8.dp))
            }
        }

        PrayerSceneHeader(
            fraction = fraction,
            expandedHeight = ExpandedHeader,
            collapsedHeight = CollapsedHeader,
            onMenuClick = { scope.launch { drawerState.open() } },
        )

        moveCandidate?.let { cand ->
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

        SceneDebugOverlay(Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp))
    }
}
