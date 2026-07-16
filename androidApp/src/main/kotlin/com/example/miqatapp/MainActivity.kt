package com.example.miqatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.miqatapp.core.focus.PhoneSilencer
import com.example.miqatapp.feature.notifications.scheduler.NotificationScheduler

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onResume() {
        super.onResume()
        PhoneSilencer.restoreIfStuck() // un-mute if a killed service never restored the ringer
        PhoneSilencer.rescheduleAll()  // re-arm prayer alarms (times may have rolled to a new day)
        NotificationScheduler.rebuildAsync() // app-open refill: top up the notification window
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}