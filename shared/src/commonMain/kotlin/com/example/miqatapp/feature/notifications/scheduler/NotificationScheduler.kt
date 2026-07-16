package com.example.miqatapp.feature.notifications.scheduler

import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.datetime.currentTime
import com.example.miqatapp.core.constants.defaults.NotificationDefaults
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.enums.NotificationType
import com.example.miqatapp.feature.miqat.domain.MiqatTime
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import com.example.miqatapp.feature.notifications.data.NotificationScheduleRepository
import com.example.miqatapp.feature.notifications.store.NotificationSettings
import com.example.miqatapp.feature.notifications.store.NotificationStore
import com.example.miqatapp.feature.notifications.store.NotificationTestStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.koin.mp.KoinPlatform
import kotlin.time.Duration.Companion.minutes

// The brain: settings + prayer times -> the nearest 63 alerts. Rebuilds on any change; writes the OS + the mirror.
object NotificationScheduler {
    private val repo: NotificationScheduleRepository by lazy { KoinPlatform.getKoin().get<NotificationScheduleRepository>() }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val mutex = Mutex()
    private var started = false

    // Call once at app start (after AppCtx on Android). Rebuilds now + on every settings/time change.
    fun start() {
        if (started) return
        started = true
        rebuildAsync()
        scope.launch {
            combine(MiqatTimesStore.today, NotificationStore.settings) { t, s -> t to s }
                .distinctUntilChanged()
                .drop(1)
                .collect { rebuild() }
        }
    }

    // Fire-and-forget rebuild (app open, receivers).
    fun rebuildAsync() { scope.launch { rebuild() } }

    // Dev test sheet: the current scheduled mirror.
    fun scheduled() = repo.observeUpcoming()

    private suspend fun rebuild() = mutex.withLock {
        val events = computeDesired().mapIndexed { i, e -> e.copy(slotId = i) }
        LocalNotifier.cancelAll()
        events.forEach { e -> val c = notificationCopy(e); LocalNotifier.schedule(e, c.title, c.body) }
        repo.replaceAll(events.map { it.toEntity() })
    }

    // Expand settings + times over the horizon, drop past, keep the nearest 63 by time.
    private fun computeDesired(): List<NotificationEvent> {
        val tz = TimeZone.currentSystemDefault()
        val settings = NotificationStore.settings.value
        val today = currentDate()
        val now = LocalDateTime(today, currentTime()).toInstant(tz).toEpochMilliseconds()
        NotificationTestStore.prunePast(now)
        // Master gate: when All alerts is off, no real alerts are scheduled (test slots still fire — dev tool).
        val real = if (!settings.allAlerts) emptyList() else (0 until NotificationDefaults.Scheduler.horizonDays).flatMap { d ->
            val date = today.plus(d, DateTimeUnit.DAY)
            eventsFor(date, MiqatTimesStore.timesFor(date), settings, tz)
        }
        val test = NotificationTestStore.items.value.map {
            NotificationEvent("test:${it.id}", "test", NotificationType.REMINDER, it.fireAtMillis)
        }
        return (real + test).filter { it.fireAtMillis > now }.sortedBy { it.fireAtMillis }.take(NotificationDefaults.Scheduler.budget)
    }

    private fun eventsFor(date: LocalDate, times: List<MiqatTime>, s: NotificationSettings, tz: TimeZone): List<NotificationEvent> = buildList {
        val friday = date.dayOfWeek == DayOfWeek.FRIDAY
        val ds = date.toString()
        fun at(m: Miqat): Long? = times.firstOrNull { it.miqat == m }?.at?.toInstant(tz)?.toEpochMilliseconds()

        // Daily prayers — on Friday the Dhuhr row yields to Jumu'ah below.
        Miqat.PRAYERS.forEach { p ->
            if (friday && p == Miqat.Dhuhr) return@forEach
            val cfg = s.prayers[p.key] ?: return@forEach
            if (!cfg.enabled) return@forEach
            val base = at(p) ?: return@forEach
            if (cfg.remindBeforeOn && cfg.remindBefore > 0) add(ev(p.key, NotificationType.REMIND_BEFORE, base - cfg.remindBefore.mins(), ds))
            if (cfg.atTime) add(ev(p.key, NotificationType.AT_TIME, base, ds))
            if (cfg.jamaat) add(ev(p.key, NotificationType.JAMAAT, base + cfg.jamaatAfter.mins(), ds))
        }
        // Jumu'ah (Friday Dhuhr)
        if (friday && s.jumuah.enabled) at(Miqat.Dhuhr)?.let { d ->
            val j = s.jumuah
            if (j.remindBeforeOn && j.remindBefore > 0) add(ev(Miqat.jumuahKey, NotificationType.REMIND_BEFORE, d - j.remindBefore.mins(), ds))
            if (j.jamaat) add(ev(Miqat.jumuahKey, NotificationType.JAMAAT, d + j.jamaatAfter.mins(), ds))
        }
        // Surahs
        if (s.mulk.enabled) at(Miqat.Isha)?.let { add(ev(NotificationTarget.MULK, NotificationType.REMINDER, it + s.mulk.afterIsha.mins(), ds)) }
        if (friday && s.kahf.enabled) {
            val k = LocalDateTime(date, LocalTime(s.kahf.hour, s.kahf.minute)).toInstant(tz).toEpochMilliseconds()
            add(ev(NotificationTarget.KAHF, NotificationType.REMINDER, k, ds))
        }
        // Dhikr
        if (s.dhikr.morningEnabled) at(Miqat.Fajr)?.let { add(ev(NotificationTarget.MORNING, NotificationType.REMINDER, it + s.dhikr.afterFajr.mins(), ds)) }
        if (s.dhikr.eveningEnabled) at(Miqat.Asr)?.let { add(ev(NotificationTarget.EVENING, NotificationType.REMINDER, it + s.dhikr.afterAsr.mins(), ds)) }
        // Nafil
        if (s.nafil.tahajjud) at(Miqat.LastThird)?.let { add(ev(NotificationTarget.TAHAJJUD, NotificationType.REMINDER, it, ds)) }
        if (s.nafil.ishraq) at(Miqat.Ishraq)?.let { add(ev(NotificationTarget.ISHRAQ, NotificationType.REMINDER, it, ds)) }
    }

    private fun ev(target: String, kind: NotificationType, fireAt: Long, date: String) =
        NotificationEvent("$target:$kind:$date", target, kind, fireAt)

    private fun Int.mins() = this.minutes.inWholeMilliseconds
}
