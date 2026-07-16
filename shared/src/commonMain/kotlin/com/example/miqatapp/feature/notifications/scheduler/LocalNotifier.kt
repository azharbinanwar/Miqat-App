package com.example.miqatapp.feature.notifications.scheduler

// OS notification primitives. No logic — the scheduler decides what and when.
expect object LocalNotifier {
    fun schedule(event: NotificationEvent)
    fun cancelAll()
}
