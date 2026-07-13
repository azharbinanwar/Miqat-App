package com.example.miqatapp.core.debug

/** Dev-only switches. Flip these off before shipping a release build. */
object Debug {

    /**
     * Home time-lapse: when `true`, Home runs a fake clock (2 days in ~48s) so the sun/moon scene and
     * the prayer flow can be watched fast — it is NOT real time. When `false`, Home uses the real clock
     * (30s tick). Keep `false` for release; flip to `true` only to test the scene/period logic.
     */
    const val FAST_CLOCK = false

    /**
     * Preview the Ramadan UI (Sehri/Iftar labels on the Home scene + Imsak row in the times list) outside
     * of actual Ramadan. When `true`, the app behaves as if the Hijri month is Ramadan. Keep `false` for release.
     */
    const val FORCE_RAMADAN = true
}
