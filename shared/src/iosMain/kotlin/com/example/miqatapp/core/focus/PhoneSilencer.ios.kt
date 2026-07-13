package com.example.miqatapp.core.focus

// iOS won't let apps touch the ringer, so nothing to do.
actual object PhoneSilencer {
    actual fun rescheduleAll() {}
    actual fun silence(startMillis: Long, endMillis: Long, label: String, mode: String) {}
    actual fun silenceFor(durationMillis: Long) {}
    actual fun restoreIfStuck() {}
    actual fun unmuteNow() {}
    actual fun extend() {}
    actual fun toggleMode() {}
}
