package com.example.miqatapp.core.focus

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri

class AndroidFocusSetup(context: Context) : FocusSetup {
    private val app = context.applicationContext
    override val supported = true

    override fun batteryUnrestricted(): Boolean {
        val pm = app.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(app.packageName)
    }

    override fun requestBatteryUnrestricted() {
        val direct = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:${app.packageName}".toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (!launch(direct)) launch(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun hasSilenceAccess() = Ringer.hasDndAccess()

    override fun requestSilenceAccess() {
        launch(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun launch(i: Intent): Boolean = try { app.startActivity(i); true } catch (_: Exception) { false }
}

@Composable
actual fun rememberFocusSetup(): FocusSetup {
    val context = LocalContext.current
    return remember(context) { AndroidFocusSetup(context) }
}
