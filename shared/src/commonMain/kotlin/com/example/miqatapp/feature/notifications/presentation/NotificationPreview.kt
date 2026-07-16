package com.example.miqatapp.feature.notifications.presentation

import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.feature.miqat.domain.MiqatTime
import kotlinx.datetime.LocalTime

// Small helpers so each Notifications tile can show WHEN its alert fires today (Focus-style), for user ease.

/** Today's clock time for a miqat point, or null if it isn't in today's list. */
fun timeOf(today: List<MiqatTime>, m: Miqat): LocalTime? = today.firstOrNull { it.miqat == m }?.at?.time

/** A base time shifted by whole minutes, wrapping past midnight. Null-safe. */
fun shift(base: LocalTime?, minutes: Int): LocalTime? =
    base?.let { LocalTime.fromSecondOfDay(((it.toSecondOfDay() + minutes * 60) % 86400 + 86400) % 86400) }

/** Offset alerts that are on, e.g. "Before 4:52 · Congregation 5:32". Null when none/base unknown. */
fun offsetSubtitle(base: LocalTime?, remindBefore: Int, jamaat: Boolean, jamaatAfter: Int, beforeLabel: String, jamaahLabel: String, pattern: String): String? {
    if (base == null) return null
    val parts = buildList {
        if (remindBefore > 0) add("$beforeLabel ${shift(base, -remindBefore)!!.format(pattern)}")
        if (jamaat) add("$jamaahLabel ${shift(base, jamaatAfter)!!.format(pattern)}")
    }
    return parts.joinToString(" · ").ifEmpty { null }
}

/** "descriptor · 9:45 PM" when on and the time is known, else just the descriptor. */
fun timeSubtitle(descriptor: String, fire: LocalTime?, enabled: Boolean, pattern: String): String =
    if (enabled && fire != null) "$descriptor · ${fire.format(pattern)}" else descriptor
