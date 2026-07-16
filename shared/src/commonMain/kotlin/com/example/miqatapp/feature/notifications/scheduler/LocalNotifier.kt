package com.example.miqatapp.feature.notifications.scheduler

// OS notification primitives. No logic — the scheduler decides what, when, and the text.
expect object LocalNotifier {
    fun schedule(event: NotificationEvent, title: String, body: String)
    fun cancelAll()
}
