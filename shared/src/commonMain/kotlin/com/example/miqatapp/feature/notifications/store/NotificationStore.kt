package com.example.miqatapp.feature.notifications.store

import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.constants.PrefConst.Field
import com.example.miqatapp.core.constants.defaults.NotificationDefaults as N
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.prefs.PrefsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** One prayer's saved alert. Keyed by the lowercased prayer name (Jumu'ah uses the key "jumuah"). */
data class PrayerAlertConfig(
    val enabled: Boolean,
    val sound: String,
    val vibrate: Boolean,
    val remindBefore: Int,
    val atTime: Boolean,
    val jamaat: Boolean,
    val jamaatAfter: Int,
)

data class JumuahConfig(val enabled: Boolean, val remindBefore: Int, val jamaatAfter: Int)
data class MulkConfig(val enabled: Boolean, val afterIsha: Int)
data class KahfConfig(val enabled: Boolean, val hour: Int, val minute: Int)
data class DhikrConfig(val morningEnabled: Boolean, val afterFajr: Int, val eveningEnabled: Boolean, val afterAsr: Int)
data class NafilConfig(val tahajjud: Boolean, val ishraq: Boolean)

/** The whole Notifications screen state in one object, so the screen reads a single flow. */
data class NotificationSettings(
    val allAlerts: Boolean,
    val prayers: Map<String, PrayerAlertConfig>,
    val jumuah: JumuahConfig,
    val mulk: MulkConfig,
    val kahf: KahfConfig,
    val dhikr: DhikrConfig,
    val nafil: NafilConfig,
)

/** Persists notification settings (per-field prefs), seeded from [NotificationDefaults]. Screens read [settings]. */
object NotificationStore {

    private val _settings = MutableStateFlow(load())
    val settings: StateFlow<NotificationSettings> = _settings.asStateFlow()

    // ── load ─────────────────────────────────────────────────
    private fun load() = NotificationSettings(
        allAlerts = PrefsService.getBoolean(PrefConst.ALL_ALERTS, N.allAlerts),
        prayers = Miqat.PRAYERS.associate { it.key to loadPrayer(it.key) },
        jumuah = JumuahConfig(
            enabled = PrefsService.getBoolean(PrefConst.alert(Miqat.jumuahKey, Field.ENABLED), N.Jumuah.enabled),
            remindBefore = PrefsService.getInt(PrefConst.alert(Miqat.jumuahKey, Field.REMIND_BEFORE), N.Jumuah.remindBefore),
            jamaatAfter = PrefsService.getInt(PrefConst.alert(Miqat.jumuahKey, Field.JAMAAT_AFTER), N.Jumuah.jamaatAfter),
        ),
        mulk = MulkConfig(
            enabled = PrefsService.getBoolean(PrefConst.SURAH_MULK, N.Mulk.enabled),
            afterIsha = PrefsService.getInt(PrefConst.SURAH_MULK_AFTER, N.Mulk.afterIsha),
        ),
        kahf = KahfConfig(
            enabled = PrefsService.getBoolean(PrefConst.SURAH_KAHF, N.Kahf.enabled),
            hour = PrefsService.getInt(PrefConst.SURAH_KAHF_HOUR, N.Kahf.hour),
            minute = PrefsService.getInt(PrefConst.SURAH_KAHF_MINUTE, N.Kahf.minute),
        ),
        dhikr = DhikrConfig(
            morningEnabled = PrefsService.getBoolean(PrefConst.DHIKR_MORNING, N.Dhikr.morningEnabled),
            afterFajr = PrefsService.getInt(PrefConst.DHIKR_MORNING_AFTER, N.Dhikr.afterFajr),
            eveningEnabled = PrefsService.getBoolean(PrefConst.DHIKR_EVENING, N.Dhikr.eveningEnabled),
            afterAsr = PrefsService.getInt(PrefConst.DHIKR_EVENING_AFTER, N.Dhikr.afterAsr),
        ),
        nafil = NafilConfig(
            tahajjud = PrefsService.getBoolean(PrefConst.NAFIL_TAHAJJUD, N.Nafil.tahajjud),
            ishraq = PrefsService.getBoolean(PrefConst.NAFIL_ISHRAQ, N.Nafil.ishraq),
        ),
    )

    private fun loadPrayer(key: String) = PrayerAlertConfig(
        enabled = PrefsService.getBoolean(PrefConst.alert(key, Field.ENABLED), N.Prayer.enabled),
        sound = PrefsService.getString(PrefConst.alert(key, Field.SOUND), N.Prayer.sound),
        vibrate = PrefsService.getBoolean(PrefConst.alert(key, Field.VIBRATE), N.Prayer.vibrate),
        remindBefore = PrefsService.getInt(PrefConst.alert(key, Field.REMIND_BEFORE), N.Prayer.remindBefore),
        atTime = PrefsService.getBoolean(PrefConst.alert(key, Field.AT_TIME), N.Prayer.atTime),
        jamaat = PrefsService.getBoolean(PrefConst.alert(key, Field.JAMAAT), N.Prayer.jamaat),
        jamaatAfter = PrefsService.getInt(PrefConst.alert(key, Field.JAMAAT_AFTER), N.Prayer.jamaatAfter),
    )

    // ── master ───────────────────────────────────────────────
    fun setAllAlerts(v: Boolean) = putBool(PrefConst.ALL_ALERTS, v) { it.copy(allAlerts = v) }

    // ── per prayer ───────────────────────────────────────────
    fun setPrayerEnabled(key: String, v: Boolean) = putPrayerBool(key, Field.ENABLED, v) { it.copy(enabled = v) }
    fun setPrayerSound(key: String, v: String) { PrefsService.putString(PrefConst.alert(key, Field.SOUND), v); updatePrayer(key) { it.copy(sound = v) } }
    fun setPrayerVibrate(key: String, v: Boolean) = putPrayerBool(key, Field.VIBRATE, v) { it.copy(vibrate = v) }
    fun setPrayerRemindBefore(key: String, v: Int) = putPrayerInt(key, Field.REMIND_BEFORE, v) { it.copy(remindBefore = v) }
    fun setPrayerAtTime(key: String, v: Boolean) = putPrayerBool(key, Field.AT_TIME, v) { it.copy(atTime = v) }
    fun setPrayerJamaat(key: String, v: Boolean) = putPrayerBool(key, Field.JAMAAT, v) { it.copy(jamaat = v) }
    fun setPrayerJamaatAfter(key: String, v: Int) = putPrayerInt(key, Field.JAMAAT_AFTER, v) { it.copy(jamaatAfter = v) }

    // ── Jumu'ah (prayer-shaped, keyed "jumuah") ──────────────
    fun setJumuahEnabled(v: Boolean) { PrefsService.putBoolean(PrefConst.alert(Miqat.jumuahKey, Field.ENABLED), v); update { it.copy(jumuah = it.jumuah.copy(enabled = v)) } }
    fun setJumuahRemindBefore(v: Int) { PrefsService.putInt(PrefConst.alert(Miqat.jumuahKey, Field.REMIND_BEFORE), v); update { it.copy(jumuah = it.jumuah.copy(remindBefore = v)) } }
    fun setJumuahJamaatAfter(v: Int) { PrefsService.putInt(PrefConst.alert(Miqat.jumuahKey, Field.JAMAAT_AFTER), v); update { it.copy(jumuah = it.jumuah.copy(jamaatAfter = v)) } }

    // ── Surahs ───────────────────────────────────────────────
    fun setMulkEnabled(v: Boolean) { PrefsService.putBoolean(PrefConst.SURAH_MULK, v); update { it.copy(mulk = it.mulk.copy(enabled = v)) } }
    fun setMulkAfter(v: Int) { PrefsService.putInt(PrefConst.SURAH_MULK_AFTER, v); update { it.copy(mulk = it.mulk.copy(afterIsha = v)) } }
    fun setKahfEnabled(v: Boolean) { PrefsService.putBoolean(PrefConst.SURAH_KAHF, v); update { it.copy(kahf = it.kahf.copy(enabled = v)) } }
    fun setKahfTime(hour: Int, minute: Int) {
        PrefsService.putInt(PrefConst.SURAH_KAHF_HOUR, hour)
        PrefsService.putInt(PrefConst.SURAH_KAHF_MINUTE, minute)
        update { it.copy(kahf = it.kahf.copy(hour = hour, minute = minute)) }
    }

    // ── Dhikr ────────────────────────────────────────────────
    fun setMorningEnabled(v: Boolean) { PrefsService.putBoolean(PrefConst.DHIKR_MORNING, v); update { it.copy(dhikr = it.dhikr.copy(morningEnabled = v)) } }
    fun setMorningAfter(v: Int) { PrefsService.putInt(PrefConst.DHIKR_MORNING_AFTER, v); update { it.copy(dhikr = it.dhikr.copy(afterFajr = v)) } }
    fun setEveningEnabled(v: Boolean) { PrefsService.putBoolean(PrefConst.DHIKR_EVENING, v); update { it.copy(dhikr = it.dhikr.copy(eveningEnabled = v)) } }
    fun setEveningAfter(v: Int) { PrefsService.putInt(PrefConst.DHIKR_EVENING_AFTER, v); update { it.copy(dhikr = it.dhikr.copy(afterAsr = v)) } }

    // ── Nafil ────────────────────────────────────────────────
    fun setTahajjud(v: Boolean) { PrefsService.putBoolean(PrefConst.NAFIL_TAHAJJUD, v); update { it.copy(nafil = it.nafil.copy(tahajjud = v)) } }
    fun setIshraq(v: Boolean) { PrefsService.putBoolean(PrefConst.NAFIL_ISHRAQ, v); update { it.copy(nafil = it.nafil.copy(ishraq = v)) } }

    // ── plumbing ─────────────────────────────────────────────
    private fun update(f: (NotificationSettings) -> NotificationSettings) { _settings.value = f(_settings.value) }
    private fun updatePrayer(key: String, g: (PrayerAlertConfig) -> PrayerAlertConfig) =
        update { it.copy(prayers = it.prayers + (key to g(it.prayers.getValue(key)))) }

    private fun putBool(pref: String, v: Boolean, f: (NotificationSettings) -> NotificationSettings) { PrefsService.putBoolean(pref, v); update(f) }
    private fun putPrayerBool(key: String, field: String, v: Boolean, g: (PrayerAlertConfig) -> PrayerAlertConfig) { PrefsService.putBoolean(PrefConst.alert(key, field), v); updatePrayer(key, g) }
    private fun putPrayerInt(key: String, field: String, v: Int, g: (PrayerAlertConfig) -> PrayerAlertConfig) { PrefsService.putInt(PrefConst.alert(key, field), v); updatePrayer(key, g) }
}
