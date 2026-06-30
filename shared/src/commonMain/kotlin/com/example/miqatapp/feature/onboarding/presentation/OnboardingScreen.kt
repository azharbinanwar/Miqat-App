package com.example.miqatapp.feature.onboarding.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Bell
import com.composables.icons.lucide.BellRing
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.Clock
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Flame
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MapPin
import com.composables.icons.lucide.MoonStar
import com.composables.icons.lucide.Settings
import com.composables.icons.lucide.Users
import com.example.miqatapp.core.navigation.AppRoute
import com.example.miqatapp.core.navigation.LocalNavController
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.affects_asr_calculation
import com.example.miqatapp.resources.allow_notifications
import com.example.miqatapp.resources.app_name
import com.example.miqatapp.resources.asr_time
import com.example.miqatapp.resources.continue_label
import com.example.miqatapp.resources.gentle_nudge_before_every_prayer
import com.example.miqatapp.resources.madhab_hanafi
import com.example.miqatapp.resources.madhab_shafi
import com.example.miqatapp.resources.miqat_logo
import com.example.miqatapp.resources.never_miss_a_prayer
import com.example.miqatapp.resources.onboarding_before_at_time_alerts
import com.example.miqatapp.resources.onboarding_city_presets
import com.example.miqatapp.resources.onboarding_gps_or_city_search
import com.example.miqatapp.resources.onboarding_jamaat_reminders
import com.example.miqatapp.resources.onboarding_jumuah_mulk_kahf
import com.example.miqatapp.resources.onboarding_live_countdown
import com.example.miqatapp.resources.onboarding_qibla_offline
import com.example.miqatapp.resources.onboarding_track_streaks
import com.example.miqatapp.resources.set_your_location
import com.example.miqatapp.resources.shadow_length_1x
import com.example.miqatapp.resources.shadow_length_2x
import com.example.miqatapp.resources.skip
import com.example.miqatapp.resources.smart_reminders
import com.example.miqatapp.resources.so_times_and_qibla_are_exact
import com.example.miqatapp.resources.start_praying
import com.example.miqatapp.resources.works_fully_offline
import com.example.miqatapp.resources.your_madhab
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Onboarding-only brand gradients (from the macOS Miqat AppColor). Not theme tokens — used nowhere else.
private val deepNavy = Color(0xFF020617)
private val purple = Color(0xFF4C1D95)
private val brown = Color(0xFF78350F)
private val asr = Color(0xFFF59E0B)
private val green = Color(0xFF14532D)
private val accentTeal = Color(0xFF0D9488)
private val deepTeal = Color(0xFF0C4A6E)
private val skyCyan = Color(0xFF0284C7)

/** Per-page chrome the container needs (background gradient + CTA). Content lives in its own page composable. */
private class PageStyle(val top: Color, val bottom: Color, val accent: Color, val cta: StringResource, val ctaIcon: ImageVector)

private val styles = listOf(
    PageStyle(deepNavy, purple, purple, Res.string.continue_label, Lucide.ChevronRight),
    PageStyle(brown, asr, brown, Res.string.continue_label, Lucide.ChevronRight),
    PageStyle(green, accentTeal, green, Res.string.allow_notifications, Lucide.ChevronRight),
    PageStyle(deepTeal, skyCyan, deepTeal, Res.string.start_praying, Lucide.Check),
)

@Composable
fun OnboardingScreen() {
    val nav = LocalNavController.current
    val pager = rememberPagerState(pageCount = { styles.size })
    val scope = rememberCoroutineScope()
    val style = styles[pager.currentPage]
    val onLast = pager.currentPage == styles.lastIndex
    var madhabIsHanafi by remember { mutableStateOf(true) }

    val finish = { nav.navigate(AppRoute.Home) { popUpTo(AppRoute.Onboarding) { inclusive = true } } }
    val top by animateColorAsState(style.top, tween(450), label = "top")
    val bottom by animateColorAsState(style.bottom, tween(450), label = "bottom")
    val accent by animateColorAsState(style.accent, tween(450), label = "accent")

    Box(
        Modifier.fillMaxSize().background(
            Brush.linearGradient(listOf(top, bottom), start = Offset.Zero, end = Offset.Infinite),
        ),
    ) {
        Column(Modifier.fillMaxSize()) {
            HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { i ->
                when (i) {
                    0 -> WelcomePage()
                    1 -> MadhabPage(madhabIsHanafi, brown) { madhabIsHanafi = it }
                    2 -> ReminderPage()
                    else -> LocationPage()
                }
            }

            Column(
                Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(styles.size) { i ->
                        Box(
                            Modifier.height(7.dp).width(if (i == pager.currentPage) 22.dp else 7.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = if (i == pager.currentPage) 1f else 0.3f)),
                        )
                    }
                }
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = 36.dp).height(50.dp)
                        .clip(RoundedCornerShape(14.dp)).background(Color.White)
                        .clickable { if (onLast) finish() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(stringResource(style.cta), color = accent, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Icon(style.ctaIcon, null, tint = accent, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        if (!onLast) {
            Text(
                stringResource(Res.string.skip),
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.TopEnd)
                    .windowInsetsPadding(WindowInsets.statusBars).padding(16.dp)
                    .clickable(onClick = finish),
            )
        }
    }
}

// ---- page content components ----

@Composable
private fun WelcomePage() = OnboardingPage(Lucide.MoonStar, isLogo = true, Res.string.app_name, Res.string.never_miss_a_prayer) {
    OnboardingBullet(Lucide.Clock, Res.string.onboarding_live_countdown)
    OnboardingBullet(Lucide.Compass, Res.string.onboarding_qibla_offline)
    OnboardingBullet(Lucide.Flame, Res.string.onboarding_track_streaks)
}

@Composable
private fun ReminderPage() = OnboardingPage(Lucide.BellRing, isLogo = false, Res.string.smart_reminders, Res.string.gentle_nudge_before_every_prayer) {
    OnboardingBullet(Lucide.Bell, Res.string.onboarding_before_at_time_alerts)
    OnboardingBullet(Lucide.MoonStar, Res.string.onboarding_jumuah_mulk_kahf)
    OnboardingBullet(Lucide.Users, Res.string.onboarding_jamaat_reminders)
}

@Composable
private fun LocationPage() = OnboardingPage(Lucide.MapPin, isLogo = false, Res.string.set_your_location, Res.string.so_times_and_qibla_are_exact) {
    OnboardingBullet(Lucide.MapPin, Res.string.onboarding_gps_or_city_search)
    OnboardingBullet(Lucide.Settings, Res.string.onboarding_city_presets)
    OnboardingBullet(Lucide.Compass, Res.string.works_fully_offline)
}

@Composable
private fun MadhabPage(isHanafi: Boolean, checkColor: Color, onSelect: (Boolean) -> Unit) =
    OnboardingPage(Lucide.Clock, isLogo = false, Res.string.your_madhab, Res.string.affects_asr_calculation) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MadhabCard(Modifier.weight(1f), Res.string.madhab_hanafi, Res.string.shadow_length_2x, isHanafi, checkColor) { onSelect(true) }
            MadhabCard(Modifier.weight(1f), Res.string.madhab_shafi, Res.string.shadow_length_1x, !isHanafi, checkColor) { onSelect(false) }
        }
    }

// ---- shared building blocks ----

@Composable
private fun OnboardingPage(
    icon: ImageVector,
    isLogo: Boolean,
    title: StringResource,
    subtitle: StringResource,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).padding(horizontal = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))
        OnboardingIcon(icon, isLogo)
        Spacer(Modifier.height(20.dp))
        Text(stringResource(title), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(stringResource(subtitle), color = Color.White.copy(alpha = 0.65f), fontSize = 15.sp, lineHeight = 21.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
        Spacer(Modifier.weight(1.2f))
    }
}

@Composable
private fun OnboardingIcon(icon: ImageVector, isLogo: Boolean) {
    Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.12f)))
        Box(Modifier.size(76.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.08f)))
        if (isLogo) {
            Image(painterResource(Res.drawable.miqat_logo), null, modifier = Modifier.size(62.dp))
        } else {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(40.dp))
        }
    }
}

@Composable
private fun OnboardingBullet(icon: ImageVector, text: StringResource) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(16.dp))
        }
        Text(stringResource(text), color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MadhabCard(modifier: Modifier, label: StringResource, desc: StringResource, selected: Boolean, checkColor: Color, onClick: () -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = if (selected) 0.2f else 0.08f))
            .border(if (selected) 2.dp else 1.dp, Color.White.copy(alpha = if (selected) 0.55f else 0.12f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 22.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            Modifier.size(26.dp).clip(CircleShape)
                .then(if (selected) Modifier.background(Color.White) else Modifier.border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape)),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Icon(Lucide.Check, null, tint = checkColor, modifier = Modifier.size(16.dp))
        }
        Text(stringResource(label), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(desc), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 15.sp)
        Row(
            Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.1f)).padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Lucide.Clock, null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(10.dp))
            Text(stringResource(Res.string.asr_time), color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
        }
    }
}
