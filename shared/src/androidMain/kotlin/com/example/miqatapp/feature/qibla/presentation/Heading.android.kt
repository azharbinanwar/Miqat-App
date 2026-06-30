package com.example.miqatapp.feature.qibla.presentation

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberHeading(): HeadingState {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val rotationSensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) }

    var state by remember { mutableStateOf(HeadingState(available = rotationSensor != null)) }

    DisposableEffect(rotationSensor) {
        if (rotationSensor == null) {
            state = HeadingState(available = false)
            return@DisposableEffect onDispose { }
        }
        val matrix = FloatArray(9)
        val orientation = FloatArray(3)
        val listener = object : SensorourEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(matrix, event.values)
                SensorManager.getOrientation(matrix, orientation)
                val deg = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
                state = state.copy(degrees = deg, available = true)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                state = state.copy(accurate = accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM)
            }
        }
        sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }
    return state
}
