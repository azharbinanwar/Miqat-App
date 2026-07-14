package com.example.miqatapp.core.notify

import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.datetime.currentTime
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.store.NotificationStore
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

/** A scheduled prayer notification. [kind] is [KIND_BEFORE]/[KIND_AT]; [label] is the English prayer name. */
data class NotifyEvent(val label: String, val kind: String, val atMillis: Long)

// Saved alert settings -> concrete notification events. Real clock (not Now), mirroring FocusWindows.
object NotifyWindows {
    const val KIND_BEFORE = "before"
    const val KIND_AT = "at"

    /** Enabled events still ahead of now (today) + all of tomorrow. Empty when the master switch is off. */
    fun upcoming(): List<NotifyEvent> {
        if (!NotificationStore.allAlerts.value) return emptyList()
        val tz = TimeZone.currentSystemDefault()
        val today = currentDate()
        val nowMillis = LocalDateTime(today, currentTime()).toInstant(tz).toEpochMilliseconds()
        return (eventsFor(today, tz) + eventsFor(today.plus(1, DateTimeUnit.DAY), tz))
            .filter { it.atMillis > nowMillis }
    }

    private fun eventsFor(date: LocalDate, tz: TimeZone): List<NotifyEvent> {
        val times = MiqatTimesStore.timesFor(date)
        val configs = NotificationStore.configs.value
        return Miqat.PRAYERS.flatMap { prayer ->
            val cfg = configs[prayer.name] ?: return@flatMap emptyList()
            if (!cfg.enabled) return@flatMap emptyList()
            val at = times.firstOrNull { it.miqat == prayer }?.at ?: return@flatMap emptyList()
            val atMs = at.toInstant(tz).toEpochMilliseconds()
            buildList {
                if (cfg.atTime) add(NotifyEvent(prayer.name, KIND_AT, atMs))
                if (cfg.remindBefore > 0) add(NotifyEvent(prayer.name, KIND_BEFORE, atMs - cfg.remindBefore * 60_000L))
            }
        }
    }
}
