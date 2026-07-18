package com.example.miqatapp

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.example.miqatapp.config.theme.AppTheme
import com.example.miqatapp.core.components.LocalOverlay
import com.example.miqatapp.core.components.OverlayState
import com.example.miqatapp.feature.settings.presentation.WidgetCustomizeSheet
import com.example.miqatapp.feature.widget.WidgetConfig
import com.example.miqatapp.feature.widget.WidgetKind
import com.example.miqatapp.feature.widget.WidgetRefresher
import com.example.miqatapp.feature.widget.WidgetStyle

// Runs when a widget is dropped on the home screen. If the gallery's Customize sheet already picked a look, we apply
// it to this instance and place it with no extra UI. Otherwise (direct Add / phone widget picker) we show the
// customize sheet over the home screen, keyed to this widget's appWidgetId — every widget owns its own look.
class PrayerCardConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        val appId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        // Customized in-app already → drop directly, no second sheet.
        if (WidgetConfig.pending() != null) {
            WidgetConfig.consumePendingInto(appId)
            place(appId)
            return
        }

        val kind = widgetKindFor(appId)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalOverlay provides remember { OverlayState() }) {
                    WidgetCustomizeSheet(
                        kind = kind,
                        initial = WidgetStyle(),
                        onConfirm = { style -> WidgetConfig.save(style, appId); place(appId) },
                        onDismiss = { finish() },
                    )
                }
            }
        }
    }

    // Confirm placement of this widget and repaint it once it's bound.
    private fun place(appId: Int) {
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appId))
        Handler(Looper.getMainLooper()).apply {
            postDelayed({ WidgetRefresher.redraw() }, 400)
            postDelayed({ WidgetRefresher.redraw() }, 1200)
        }
        finish()
    }

    // Which widget was dropped, from its provider class name.
    private fun widgetKindFor(appId: Int): WidgetKind {
        val name = AppWidgetManager.getInstance(this).getAppWidgetInfo(appId)?.provider?.className ?: ""
        return when {
            name.contains("Minimal") -> WidgetKind.Minimal
            name.contains("Card") -> WidgetKind.Card
            name.contains("Times") -> WidgetKind.Times
            name.contains("Bar") -> WidgetKind.Bar
            name.contains("Next") -> WidgetKind.Current
            name.contains("Tile") -> WidgetKind.Tile
            name.contains("Icon") -> WidgetKind.Icon
            else -> WidgetKind.Card
        }
    }
}
