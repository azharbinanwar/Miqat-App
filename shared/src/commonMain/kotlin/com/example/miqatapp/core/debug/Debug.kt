package com.example.miqatapp.core.debug

import kotlinx.datetime.LocalDate

/** Dev-only switches. Flip these off before shipping a release build. */
object Debug {

    /**
     * Home time-lapse: when `true`, Home runs a fake clock (2 days in ~48s) so the sun/moon scene and
     * the prayer flow can be watched fast — it is NOT real time. When `false`, Home uses the real clock
     * (30s tick). Keep `false` for release; flip to `true` only to test the scene/period logic.
     */
    const val FAST_CLOCK = false

    /**
     * Time machine for testing seasons without waiting a year. Set a date (e.g. LocalDate(2026, 12, 21) for the
     * winter solstice, or six months out) and Home treats it as today, so you see that day's sun/moon scene and
     * prayer times for the currently-set location. Combine with FAST_CLOCK to run the day fast. null = real today.
     */
//    val DATE_OVERRIDE: LocalDate = LocalDate(2027, 2, 16)
    val DATE_OVERRIDE: LocalDate? = null
}
