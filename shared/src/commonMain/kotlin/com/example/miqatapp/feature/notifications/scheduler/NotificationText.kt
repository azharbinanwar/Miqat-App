package com.example.miqatapp.feature.notifications.scheduler

import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.NotificationType
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.evening_adhkar
import com.example.miqatapp.resources.ishraq
import com.example.miqatapp.resources.morning_adhkar
import com.example.miqatapp.resources.notif_evening_body
import com.example.miqatapp.resources.notif_ishraq_body
import com.example.miqatapp.resources.notif_jumuah_before_body
import com.example.miqatapp.resources.notif_jumuah_before_title
import com.example.miqatapp.resources.notif_jumuah_jamaat_body
import com.example.miqatapp.resources.notif_jumuah_jamaat_title
import com.example.miqatapp.resources.notif_kahf_body
import com.example.miqatapp.resources.notif_morning_body
import com.example.miqatapp.resources.notif_mulk_body
import com.example.miqatapp.resources.notif_prayer_at_body
import com.example.miqatapp.resources.notif_prayer_at_title
import com.example.miqatapp.resources.notif_prayer_before_body
import com.example.miqatapp.resources.notif_prayer_before_title
import com.example.miqatapp.resources.notif_prayer_jamaat_body
import com.example.miqatapp.resources.notif_prayer_jamaat_title
import com.example.miqatapp.resources.notif_tahajjud_body
import com.example.miqatapp.resources.notifications
import com.example.miqatapp.resources.prayer_jumuah
import com.example.miqatapp.resources.surah_al_kahf
import com.example.miqatapp.resources.surah_al_mulk
import com.example.miqatapp.resources.tahajjud
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

// The text a notification shows: first line + second line.
data class NotificationCopy(val title: String, val body: String)

// target -> its display-name resource. Prayers resolve via Miqat; the rest are fixed.
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

// Build the localized title+body for one event. Called at schedule time (prayer/n/time all in hand);
// the OS then just displays the strings, so no i18n or formatting happens at fire time.
suspend fun notificationCopy(e: NotificationEvent): NotificationCopy {
    if (e.target == "test") return NotificationCopy("Test #${e.eventKey.substringAfterLast(':')}", "")
    val jumuah = e.target == Miqat.jumuahKey
    val name = getString(labelRes(e.target))
    return when (e.kind) {
        NotificationType.AT_TIME ->
            NotificationCopy(getString(Res.string.notif_prayer_at_title, name), getString(Res.string.notif_prayer_at_body, timeStr(e.fireAtMillis)))
        NotificationType.REMIND_BEFORE ->
            if (jumuah) NotificationCopy(getString(Res.string.notif_jumuah_before_title), getString(Res.string.notif_jumuah_before_body))
            else NotificationCopy(getString(Res.string.notif_prayer_before_title, name), getString(Res.string.notif_prayer_before_body))
        NotificationType.JAMAAT ->
            if (jumuah) NotificationCopy(getString(Res.string.notif_jumuah_jamaat_title), getString(Res.string.notif_jumuah_jamaat_body))
            else NotificationCopy(getString(Res.string.notif_prayer_jamaat_title), getString(Res.string.notif_prayer_jamaat_body))
        NotificationType.REMINDER -> NotificationCopy(name, getString(bodyRes(e.target)))
    }
}

// The one-line body for a fixed-target reminder (surah / dhikr / nafil).
private fun bodyRes(target: String): StringResource = when (target) {
    NotificationTarget.MULK -> Res.string.notif_mulk_body
    NotificationTarget.KAHF -> Res.string.notif_kahf_body
    NotificationTarget.MORNING -> Res.string.notif_morning_body
    NotificationTarget.EVENING -> Res.string.notif_evening_body
    NotificationTarget.TAHAJJUD -> Res.string.notif_tahajjud_body
    else -> Res.string.notif_ishraq_body
}

// Clock time of fireAt in the user's 12/24 setting, for the at-prayer body.
private fun timeStr(millis: Long): String {
    val t = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault()).time
    return t.format(SettingsStore.timeFormat.value.pattern)
}
