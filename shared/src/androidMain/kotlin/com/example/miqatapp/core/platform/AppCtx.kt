package com.example.miqatapp.core.platform

import android.content.Context

/** App context for background code (scheduler/receivers) that has no Composable to read LocalContext from. */
object AppCtx {
    lateinit var context: Context
}
