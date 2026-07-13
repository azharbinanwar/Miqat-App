package com.example.miqatapp.core.focus

// Mutes the phone for a window via a foreground service (survives swipe-kill). No-op on iOS.
expect object PhoneSilencer {
    fun rescheduleAll()                                                          // arm alarms for every enabled prayer window + saved test slots
    fun silence(startMillis: Long, endMillis: Long, label: String, mode: String) // start the service for a window (from the alarm)
    fun silenceFor(durationMillis: Long)                           // mute now for a duration (quick test)
    fun restoreIfStuck()                                           // call on app open: heal a mute the killed service never restored
    fun unmuteNow()                                                // notification "Unmute now": restore + stop the service early
    fun extend()                                                   // notification "+5 min": push the current window's end out
    fun toggleMode()                                               // notification mode button: flip Silent <-> Vibrate mid-slot
}
