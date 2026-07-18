package com.example.miqatapp

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.feature.settings.presentation.WidgetSettingsScreen
import com.example.miqatapp.feature.widget.WidgetRefresher

// Launched when the Prayer Card is dropped from the widget gallery: hosts our in-app editor so setup feels native.
// Back = cancel (widget not added); primary button confirms placement.
class PrayerCardConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val appId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        setContent {
            AppTheme {
                WidgetSettingsScreen(
                    onBack = { finish() },
                    onPrimary = {
                        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appId))
                        // The new widget binds just after we return OK — repaint once it's up so it shows
                        // the picked look immediately (its first draw can land before the config is picked up).
                        Handler(Looper.getMainLooper()).apply {
                            postDelayed({ WidgetRefresher.redraw() }, 400)
                            postDelayed({ WidgetRefresher.redraw() }, 1200)
                        }
                        finish()
                    },
                )
            }
        }
    }
}
