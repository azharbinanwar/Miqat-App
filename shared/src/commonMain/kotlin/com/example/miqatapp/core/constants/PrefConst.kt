package com.example.miqatapp.core.constants

/**
 * One place for every persisted-preference key. The storage layer (DataStore/Settings) isn't wired yet —
 * this is the single source of truth so screens and the future store agree on names. Per-prayer keys are
 * built with the helpers so prayer names never get hardcoded twice.
 *
 * ponytail: keys only; the actual read/write store comes with persistence.
 */
object PrefConst {

    // ── General ──────────────────────────────────────────────
    const val THEME = "theme"                 // light | dark | system
    const val LANGUAGE = "language"           // en | ar | null = system
    const val TIME_FORMAT = "time_format"     // 12 | 24
    const val HIJRI_OFFSET = "hijri_offset"   // -2..+2
    const val SEHRI_REFERENCE = "sehri_reference" // Fajr | Imsak — which time the Ramadan "Sehri" label uses

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

    // ── Qibla ────────────────────────────────────────────────
    const val QIBLA_STYLE = "qibla_style"     // enum name: Modern | Classic | CompassRose

    // ── Notifications ────────────────────────────────────────
    const val ALL_ALERTS = "notif_all_alerts"
    /** Per-prayer alert field, e.g. notif_fajr_enabled / _remind_before / _at_time / _jamaat / _jamaat_after.
     *  The prayer segment comes from Miqat (name, or Miqat.jumuahKey for Jumu'ah). */
    fun alert(prayer: String, field: String) = "notif_${prayer.lowercase()}_$field"

    // Surahs
    const val SURAH_MULK = "notif_mulk"
    const val SURAH_MULK_AFTER = "notif_mulk_after"
    const val SURAH_KAHF = "notif_kahf"
    const val SURAH_KAHF_HOUR = "notif_kahf_hour"
    const val SURAH_KAHF_MINUTE = "notif_kahf_minute"

    // Nafil
    const val NAFIL_TAHAJJUD = "notif_tahajjud"
    const val NAFIL_ISHRAQ = "notif_ishraq"

    // Dhikr
    const val DHIKR_MORNING = "notif_morning_adhkar"
    const val DHIKR_MORNING_AFTER = "notif_morning_adhkar_after"
    const val DHIKR_EVENING = "notif_evening_adhkar"
    const val DHIKR_EVENING_AFTER = "notif_evening_adhkar_after"
    const val DHIKR_TASBIH_NUDGE = "notif_tasbih_nudge" // ponytail: kept, tasbih nudge revisited later
    const val NOTIF_TEST_SLOTS = "notif_test_slots"     // dev-only one-shot test notifications (JSON)

    // ── Prayer Focus (auto-silence) ──────────────────────────
    /** Per-prayer focus field, e.g. focus_maghrib_enabled / _start_after / _duration. */
    fun focus(prayer: String, field: String) = "focus_${prayer.lowercase()}_$field"
    const val FOCUS_SAVED_RINGER = "focus_saved_ringer"   // ringer mode saved before muting, to put back after
    const val FOCUS_SILENCE_END = "focus_silence_end"     // epoch millis (string) of the active mute window's end
    const val FOCUS_SILENCE_MODE = "focus_silence_mode"   // active window's mode (Silent | Vibrate), for catch-up
    const val FOCUS_SILENCE_LABEL = "focus_silence_label" // active window's prayer name, for extend/mode restart
    const val FOCUS_TEST_SLOTS = "focus_test_slots"       // saved one-shot test slots (JSON), survive reboot

    // ── Widget ───────────────────────────────────────────────
    const val WIDGET_STYLE = "widget_style_" // per-instance style JSON, suffixed with appWidgetId

    // ── Quran ────────────────────────────────────────────────
    const val QURAN_FONT_SP = "quran_font_sp" // reading font size (sp); last-read page & bookmarks come later
    const val QURAN_FONT = "quran_font"       // mushaf body font (QuranFont enum name)
    const val QURAN_FAVORITES = "quran_favorites" // favorite surah numbers, comma-separated

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
        const val REMIND_BEFORE_ON = "remind_before_on"
        const val REMIND_BEFORE = "remind_before"
        const val AT_TIME = "at_time"
        const val JAMAAT = "jamaat"
        const val JAMAAT_AFTER = "jamaat_after"
        const val START_AFTER = "start_after"
        const val DURATION = "duration"
        const val SILENCE_MODE = "silence_mode"
    }
}
