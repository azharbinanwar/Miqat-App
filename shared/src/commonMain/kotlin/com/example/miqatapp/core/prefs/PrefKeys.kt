package com.example.miqatapp.core.prefs

/**
 * One place for every persisted-preference key. The storage layer (DataStore/Settings) isn't wired yet —
 * this is the single source of truth so screens and the future store agree on names. Per-prayer keys are
 * built with the helpers so prayer names never get hardcoded twice.
 *
 * ponytail: keys only; the actual read/write store comes with persistence.
 */
object PrefKeys {

    // ── General ──────────────────────────────────────────────
    const val THEME = "theme"                 // light | dark | system
    const val LANGUAGE = "language"           // en | ar | null = system
    const val TIME_FORMAT = "time_format"     // 12 | 24
    const val HIJRI_OFFSET = "hijri_offset"   // -2..+2

    // ── Prayer calculation ───────────────────────────────────
    const val CALC_METHOD = "calc_method"
    const val MADHAB = "madhab"
    const val HIGH_LAT_RULE = "high_lat_rule"
    const val CUSTOM_FAJR_ANGLE = "custom_fajr_angle" // used when CALC_METHOD = Custom
    const val CUSTOM_ISHA_ANGLE = "custom_isha_angle"
    fun adjust(prayer: String) = "adjust_${prayer.lowercase()}" // ± minutes per prayer

    // ── Location ─────────────────────────────────────────────
    const val ACTIVE_PLACE = "active_place"   // the selected Place, as JSON
    const val SAVED_PLACES = "saved_places"   // saved Places (favorites), as a JSON array

    // ── Notifications ────────────────────────────────────────
    const val ALL_ALERTS = "notif_all_alerts"
    /** Per-prayer alert field, e.g. notif_fajr_enabled / _sound / _vibrate / _remind_before / _at_time / _jamaat / _jamaat_after. */
    fun alert(prayer: String, field: String) = "notif_${prayer.lowercase()}_$field"
    const val JUMUAH_BEFORE = "notif_jumuah_before"
    const val JUMUAH_JAMAAT = "notif_jumuah_jamaat"
    const val JUMUAH_MISSED = "notif_jumuah_missed"
    const val SUNNAH_MULK = "notif_mulk"
    const val SUNNAH_MULK_AFTER = "notif_mulk_after"
    const val SUNNAH_KAHF = "notif_kahf"
    const val SUNNAH_TAHAJJUD = "notif_tahajjud"
    const val SUNNAH_DUHA = "notif_duha"
    const val DHIKR_MORNING = "notif_morning_adhkar"
    const val DHIKR_EVENING = "notif_evening_adhkar"
    const val DHIKR_TASBIH_NUDGE = "notif_tasbih_nudge"
    const val BEHAVIOUR_RESPECT_DND = "notif_respect_dnd"
    const val BEHAVIOUR_SNOOZE = "notif_snooze"
    const val BEHAVIOUR_I_PRAYED = "notif_i_prayed"

    // ── Prayer Focus (auto-silence) ──────────────────────────
    /** Per-prayer focus field, e.g. focus_maghrib_enabled / _start_after / _duration. */
    fun focus(prayer: String, field: String) = "focus_${prayer.lowercase()}_$field"

    // ── Tasbih ───────────────────────────────────────────────
    const val TASBIH_MODE = "tasbih_mode"       // beads | tap | focus
    const val TASBIH_SIZE = "tasbih_size"
    const val TASBIH_SHAPE = "tasbih_shape"
    const val TASBIH_FINISH = "tasbih_finish"
    const val TASBIH_COLOR = "tasbih_color"
    const val TASBIH_VIBRATE = "tasbih_vibrate"
    const val TASBIH_SOUND = "tasbih_sound"

    // common field names for the per-prayer helpers
    object Field {
        const val ENABLED = "enabled"
        const val SOUND = "sound"
        const val VIBRATE = "vibrate"
        const val REMIND_BEFORE = "remind_before"
        const val AT_TIME = "at_time"
        const val JAMAAT = "jamaat"
        const val JAMAAT_AFTER = "jamaat_after"
        const val START_AFTER = "start_after"
        const val DURATION = "duration"
    }
}
