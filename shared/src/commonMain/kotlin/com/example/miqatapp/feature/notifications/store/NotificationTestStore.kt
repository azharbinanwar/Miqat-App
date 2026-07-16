package com.example.miqatapp.feature.notifications.store

import com.example.miqatapp.core.constants.PrefConst
import com.example.miqatapp.core.prefs.PrefsService
import com.example.miqatapp.feature.notifications.scheduler.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// One dev-only test notification. `id` keeps each unique even if two share a fire time. `label` is a display string.
@Serializable
data class TestNotification(val id: Long, val fireAtMillis: Long, val label: String)

// Dev-only custom notifications. Persisted; the scheduler merges them into the real schedule (respects the budget).
object NotificationTestStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val _items = MutableStateFlow(load())
    val items: StateFlow<List<TestNotification>> = _items.asStateFlow()

    fun add(fireAtMillis: Long, label: String) {
        val id = (_items.value.maxOfOrNull { it.id } ?: 0L) + 1L
        commit(_items.value + TestNotification(id, fireAtMillis, label))
    }
    fun remove(item: TestNotification) = commit(_items.value - item)

    // Drop fired ones. Called from rebuild, so it must NOT rebuild (would recurse).
    fun prunePast(nowMillis: Long) {
        val kept = _items.value.filter { it.fireAtMillis > nowMillis }
        if (kept.size != _items.value.size) { _items.value = kept; persist(kept) }
    }

    private fun commit(list: List<TestNotification>) {
        _items.value = list
        persist(list)
        NotificationScheduler.rebuildAsync()
    }

    private fun persist(list: List<TestNotification>) =
        PrefsService.putString(PrefConst.NOTIF_TEST_SLOTS, json.encodeToString(list))

    private fun load(): List<TestNotification> =
        PrefsService.getStringOrNull(PrefConst.NOTIF_TEST_SLOTS)
            ?.let { runCatching { json.decodeFromString<List<TestNotification>>(it) }.getOrNull() } ?: emptyList()
}
