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
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.prayer_asr
import com.example.miqatapp.resources.prayer_dhuhr
import com.example.miqatapp.resources.prayer_fajr
import com.example.miqatapp.resources.prayer_isha
import com.example.miqatapp.resources.prayer_maghrib
import com.example.miqatapp.resources.prayer_sunrise
import org.jetbrains.compose.resources.StringResource

/** The five daily prayers + sunrise. Icon + localized label here; colors live in AppColors (theme-aware). */
enum class Prayer(val icon: ImageVector, val labelRes: StringResource) {
    Fajr(Lucide.Sunrise, Res.string.prayer_fajr),
    Sunrise(Lucide.SunMedium, Res.string.prayer_sunrise),
    Dhuhr(Lucide.Sun, Res.string.prayer_dhuhr),
    Asr(Lucide.CloudSun, Res.string.prayer_asr),
    Maghrib(Lucide.Sunset, Res.string.prayer_maghrib),
    Isha(Lucide.Moon, Res.string.prayer_isha),
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
