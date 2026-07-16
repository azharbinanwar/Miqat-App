package com.example.miqatapp.core.constants.defaults

import com.example.miqatapp.core.enums.Miqat

/** One prayer's auto-silence window in minutes: start [after] the prayer, stay silent [duration], capped by [max]. */
data class FocusSlot(val after: Int, val duration: Int, val max: Int)

/** A configurable focus row: a prayer, or Jumu'ah (the Friday Dhuhr). [key] is the pref key; label/icon come from [miqat]. */
data class FocusRow(val key: String, val miqat: Miqat, val friday: Boolean, val default: FocusSlot)

/**
 * The configurable Prayer-Focus set and default windows: the five daily prayers plus Jumu'ah (longer, Friday).
 * Only the numbers live here; labels and icons come from Miqat.
 */
object FocusDefaults {
    val rows: List<FocusRow> = Miqat.PRAYERS.map { FocusRow(it.key, it, friday = false, slotFor(it)) } +
        FocusRow(Miqat.jumuahKey, Miqat.Dhuhr, friday = true, FocusSlot(after = 45, duration = 60, max = 180))

    private fun slotFor(prayer: Miqat): FocusSlot = when (prayer) {
        Miqat.Fajr -> FocusSlot(after = 30, duration = 30, max = 120)
        Miqat.Dhuhr -> FocusSlot(after = 20, duration = 30, max = 120)
        Miqat.Maghrib -> FocusSlot(after = 0, duration = 15, max = 120)
        else -> FocusSlot(after = 5, duration = 30, max = 120) // Asr, Isha
    }
}
