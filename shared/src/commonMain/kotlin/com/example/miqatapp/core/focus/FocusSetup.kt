package com.example.miqatapp.core.focus

import androidx.compose.runtime.Composable

// Battery + auto-start toggles so the service survives on aggressive OEMs (Vivo/Xiaomi). No-op on iOS.
interface FocusSetup {
    val supported: Boolean
    fun batteryUnrestricted(): Boolean
    fun requestBatteryUnrestricted()
    fun hasSilenceAccess(): Boolean // Do Not Disturb access, needed for Silent (Vibrate needs nothing)
    fun requestSilenceAccess()
}

@Composable
expect fun rememberFocusSetup(): FocusSetup
