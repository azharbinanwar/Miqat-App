package com.example.miqatapp.core.store

import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.focus.PhoneSilencer
import com.example.miqatapp.core.prefs.PrefsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// A one-shot test mute window. `label` is a display string (e.g. "5:04 for 2 min").
@Serializable
data class TestSlot(val start: Long, val end: Long, val mode: String, val label: String)

// Saved test slots for the Background test tool. Persisted so they survive a reboot (re-armed by rescheduleAll).
// Dev-only; not part of the real prayer flow.
object FocusTestStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val _slots = MutableStateFlow(load())
    val slots: StateFlow<List<TestSlot>> = _slots.asStateFlow()

    fun add(slot: TestSlot) = commit(_slots.value + slot)
    fun remove(slot: TestSlot) = commit(_slots.value - slot)

    // Drop finished slots. Called from rescheduleAll, so it must NOT re-arm (would recurse).
    fun prunePast(nowMillis: Long) {
        val kept = _slots.value.filter { it.end > nowMillis }
        if (kept.size != _slots.value.size) { _slots.value = kept; persist(kept) }
    }

    private fun commit(list: List<TestSlot>) {
        _slots.value = list
        persist(list)
        PhoneSilencer.rescheduleAll() // re-arm real + test alarms (no-op on iOS)
    }

    private fun persist(list: List<TestSlot>) =
        PrefsService.putString(PrefConst.FOCUS_TEST_SLOTS, json.encodeToString(list))

    private fun load(): List<TestSlot> =
        PrefsService.getStringOrNull(PrefConst.FOCUS_TEST_SLOTS)
            ?.let { runCatching { json.decodeFromString<List<TestSlot>>(it) }.getOrNull() } ?: emptyList()
}
