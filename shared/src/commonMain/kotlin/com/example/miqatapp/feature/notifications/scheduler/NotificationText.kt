package com.example.miqatapp.feature.notifications.scheduler

import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.evening_adhkar
import com.example.miqatapp.resources.ishraq
import com.example.miqatapp.resources.morning_adhkar
import com.example.miqatapp.resources.notifications
import com.example.miqatapp.resources.prayer_jumuah
import com.example.miqatapp.resources.surah_al_kahf
import com.example.miqatapp.resources.surah_al_mulk
import com.example.miqatapp.resources.tahajjud
import org.jetbrains.compose.resources.StringResource

// target -> its display-name resource. Prayers resolve via Miqat; the rest are fixed. Resolved to text at fire time.
fun labelRes(target: String): StringResource = when (target) {
    Miqat.jumuahKey -> Res.string.prayer_jumuah
    NotificationTarget.MULK -> Res.string.surah_al_mulk
    NotificationTarget.KAHF -> Res.string.surah_al_kahf
    NotificationTarget.MORNING -> Res.string.morning_adhkar
    NotificationTarget.EVENING -> Res.string.evening_adhkar
    NotificationTarget.TAHAJJUD -> Res.string.tahajjud
    NotificationTarget.ISHRAQ -> Res.string.ishraq
    else -> Miqat.PRAYERS.firstOrNull { it.key == target }?.labelRes ?: Res.string.notifications
}
