package com.example.miqatapp.core.constants.defaults

/**
 * Default values for every notification setting, one place to manage them. Sections match the
 * Notifications screen. The store reads these; only the numbers live here.
 */
object NotificationDefaults {

    /** Master switch for every reminder. */
    const val allAlerts = true               // true = all alerts on out of the box

    /** Per-prayer alert. Same defaults for all five daily prayers. */
    object Prayer {
        const val enabled = false            // prayer alert off until the user turns it on
        const val sound = "Full adhan"       // canonical sound code, label resolves in the UI
        const val vibrate = true             // buzz with the alert (Android only)
        const val remindBefore = 15          // minutes before the prayer to nudge
        const val remindBeforeMin = 0        // lowest the "remind before" stepper allows
        const val remindBeforeMax = 60       // highest the "remind before" stepper allows
        const val atTime = true              // also fire right at the prayer time
        const val jamaat = false             // second reminder for the congregation time
        const val jamaatAfter = 20           // minutes after the start for jamaat
        const val jamaatAfterMin = 5         // lowest jamaat offset
        const val jamaatAfterMax = 60        // highest jamaat offset
        const val step = 5                   // stepper jump per tap (minutes)
    }

    /** Friday Jumu'ah. */
    object Jumuah {
        const val enabled = false            // Jumu'ah alert off by default
        const val remindBefore = 30          // minutes before Jumu'ah to nudge
        const val remindBeforeMin = 15       // lowest "remind before"
        const val remindBeforeMax = 120      // highest "remind before"
        const val jamaatAfter = 20           // minutes after start for jamaat
        const val jamaatAfterMin = 5         // lowest jamaat offset
        const val jamaatAfterMax = 60        // highest jamaat offset
        const val remindIfMissed = true      // ping again if not marked prayed
        const val step = 5                   // stepper jump per tap (minutes)
    }

    /** Surah Al-Mulk, nightly after Isha. */
    object Mulk {
        const val enabled = true             // Mulk reminder on by default
        const val afterIsha = 30             // minutes after Isha to remind
        const val afterIshaMin = 5           // lowest offset
        const val afterIshaMax = 120         // highest offset
        const val step = 5                   // stepper jump per tap (minutes)
    }

    /** Surah Al-Kahf, Friday at a chosen clock time. */
    object Kahf {
        const val enabled = true             // Kahf reminder on by default
        const val hour = 10                  // default reminder hour (24h)
        const val minute = 0                 // default reminder minute
    }

    /** Morning and evening adhkar, offset after Fajr / Asr. */
    object Dhikr {
        const val morningEnabled = true      // morning adhkar on by default
        const val afterFajr = 15             // minutes after Fajr for morning adhkar
        const val eveningEnabled = true      // evening adhkar on by default
        const val afterAsr = 15              // minutes after Asr for evening adhkar
        const val offsetMin = 0              // lowest offset for either
        const val offsetMax = 60             // highest offset for either
        const val step = 5                   // stepper jump per tap (minutes)
    }

    /** Nafil prayers. */
    object Nafil {
        const val tahajjud = false           // Tahajjud reminder off by default
        const val ishraq = false             // Ishraq reminder off by default
    }

    /** Available adhan sounds (canonical codes, labels resolve in the UI). */
    val sounds = listOf("Full adhan", "Takbir", "Beep", "Silent")
}
