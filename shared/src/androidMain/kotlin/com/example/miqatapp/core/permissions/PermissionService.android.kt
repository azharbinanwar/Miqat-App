package com.example.miqatapp.core.permissions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
actual fun rememberPermissionService(): PermissionService {
    val context = LocalContext.current
    // Bridges the launcher's async callback back to the suspending request() call.
    val pending = remember { mutableStateOf<Continuation<Unit>?>(null) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        pending.value?.resume(Unit)
        pending.value = null
    }
    return remember(context) { AndroidPermissionService(context, launcher::launch, pending) }
}

private class AndroidPermissionService(
    private val context: Context,
    private val launch: (Array<String>) -> Unit,
    private val pending: MutableState<Continuation<Unit>?>,
) : PermissionService {

    override suspend fun status(permission: AppPermission): PermissionStatus {
        if (isGranted(permission)) return PermissionStatus.Granted
        val perms = runtimePermissions(permission) ?: return PermissionStatus.Granted
        val activity = context.findActivity()
        // Android can't tell "never asked" from "don't ask again". rationale==true means we've been
        // refused at least once (Denied); otherwise treat as NotDetermined until the first denial.
        val refusedBefore = activity != null && perms.any { activity.shouldShowRequestPermissionRationale(it) }
        return if (refusedBefore) PermissionStatus.Denied else PermissionStatus.NotDetermined
    }

    override suspend fun request(permission: AppPermission): PermissionStatus {
        if (isGranted(permission)) return PermissionStatus.Granted
        val perms = runtimePermissions(permission) ?: return PermissionStatus.Granted
        suspendCoroutine<Unit> { cont ->
            pending.value = cont
            launch(perms)
        }
        if (isGranted(permission)) return PermissionStatus.Granted
        val activity = context.findActivity()
        val permanently = activity != null && perms.none { activity.shouldShowRequestPermissionRationale(it) }
        return if (permanently) PermissionStatus.DeniedPermanently else PermissionStatus.Denied
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun isGranted(permission: AppPermission): Boolean = when (permission) {
        // Coarse is sufficient for prayer times + Qibla; granted if either is held.
        AppPermission.Location ->
            granted(android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                granted(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        AppPermission.Notifications ->
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                granted(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun granted(perm: String) =
        context.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED

    /** null = no runtime permission to request (notifications on API < 33). */
    private fun runtimePermissions(permission: AppPermission): Array<String>? = when (permission) {
        AppPermission.Location -> arrayOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        AppPermission.Notifications ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
            } else null
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
