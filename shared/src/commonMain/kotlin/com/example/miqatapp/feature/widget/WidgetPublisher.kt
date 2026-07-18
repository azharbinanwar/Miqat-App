package com.example.miqatapp.feature.widget

import com.example.miqatapp.core.datetime.HijriMonth
import com.example.miqatapp.core.datetime.currentDate
import com.example.miqatapp.core.datetime.format
import com.example.miqatapp.core.datetime.labelRes
import com.example.miqatapp.core.enums.Miqat
import com.example.miqatapp.core.store.LocationStore
import com.example.miqatapp.core.store.SettingsStore
import com.example.miqatapp.feature.miqat.domain.MiqatTime
import com.example.miqatapp.feature.miqat.store.MiqatTimesStore
import com.example.miqatapp.resources.Res
import com.example.miqatapp.resources.prayer_jumuah
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString

/** Turns today's times + settings into the [WidgetSnapshot] and refreshes the widget. Started once at app start. */
object WidgetPublisher {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var started = false

    fun start() {
        if (started) return
        started = true
        refresh()
        scope.launch {
            combine(MiqatTimesStore.today, SettingsStore.timeFormat, SettingsStore.language) { t, f, l -> Triple(t, f, l) }
                .distinctUntilChanged()
                .drop(1)
                .collect { refresh() }
        }
    }

    /** Rebuild the snapshot for the current day and push it. Safe to call from the transition alarm (handles rollover). */
    fun refresh() {
        WidgetStore.write(Json.encodeToString(build()))
        WidgetRefresher.refresh()
    }

    private fun build(): WidgetSnapshot {
        val tz = TimeZone.currentSystemDefault()
        val today = currentDate()
        val friday = today.dayOfWeek == DayOfWeek.FRIDAY
        val pattern = SettingsStore.timeFormat.value.pattern
        val todayTimes = MiqatTimesStore.today.value

        fun row(t: MiqatTime): WidgetPrayer {
            val res = if (friday && t.miqat == Miqat.Dhuhr) Res.string.prayer_jumuah else t.miqat.labelRes
            return WidgetPrayer(t.miqat.key, t.at.toInstant(tz).toEpochMilliseconds(), t.at.format(pattern), runBlocking { getString(res) }, arabicLabel(res))
        }

        val five = Miqat.PRAYERS.mapNotNull { p -> todayTimes.firstOrNull { it.miqat == p }?.let(::row) }
        val segments = Miqat.SLOTS.mapNotNull { p -> todayTimes.firstOrNull { it.miqat == p }?.let(::row) }
        val yesterdayIsha = MiqatTimesStore.timesFor(today.plus(-1, DateTimeUnit.DAY)).first { it.miqat == Miqat.Isha }
        val tomorrowFajr = MiqatTimesStore.timesFor(today.plus(1, DateTimeUnit.DAY)).first { it.miqat == Miqat.Fajr }
        val h = SettingsStore.hijriDate.value
        val hijri = "${h.day} " + runBlocking { getString(HijriMonth.of(h.month).labelRes) }
        val dayName = runBlocking { getString(today.dayOfWeek.labelRes) }
        return WidgetSnapshot(today.toString(), LocationStore.activePlace.value.name, hijri, dayName, five, segments, row(yesterdayIsha), row(tomorrowFajr))
    }
}
