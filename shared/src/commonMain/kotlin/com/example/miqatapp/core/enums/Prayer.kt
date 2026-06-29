package com.example.miqatapp.core.enums

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.composables.icons.lucide.CloudSun
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.Sun
import com.composables.icons.lucide.SunMedium
import com.composables.icons.lucide.Sunrise
import com.composables.icons.lucide.Sunset
import com.example.miqatapp.config.theme.AppTheme

/** The five daily prayers + sunrise. Icon here; colors live in AppColors (theme-aware). */
enum class Prayer(val icon: ImageVector) {
    Fajr(Lucide.Sunrise),
    Sunrise(Lucide.SunMedium),
    Dhuhr(Lucide.Sun),
    Asr(Lucide.CloudSun),
    Maghrib(Lucide.Sunset),
    Isha(Lucide.Moon),
}

val Prayer.color: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            Prayer.Fajr -> it.fajrColor
            Prayer.Sunrise -> it.sunriseColor
            Prayer.Dhuhr -> it.dhuhrColor
            Prayer.Asr -> it.asrColor
            Prayer.Maghrib -> it.maghribColor
            Prayer.Isha -> it.ishaColor
        }
    }

val Prayer.onColor: Color
    @Composable get() = AppTheme.colors.let {
        when (this) {
            Prayer.Fajr -> it.onFajrColor
            Prayer.Sunrise -> it.onSunriseColor
            Prayer.Dhuhr -> it.onDhuhrColor
            Prayer.Asr -> it.onAsrColor
            Prayer.Maghrib -> it.onMaghribColor
            Prayer.Isha -> it.onIshaColor
        }
    }
