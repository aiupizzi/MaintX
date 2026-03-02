package com.maintx.feature.diagnostics

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TorchController @Inject constructor(
    @ApplicationContext context: Context
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    private val torchCameraId: String? by lazy {
        cameraManager.cameraIdList.firstOrNull { id ->
            val characteristics = cameraManager.getCameraCharacteristics(id)
            characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    }

    fun setTorch(enabled: Boolean) {
        val cameraId = torchCameraId ?: return
        try {
            cameraManager.setTorchMode(cameraId, enabled)
        } catch (_: CameraAccessException) {
            // best effort on unsupported devices
        } catch (_: IllegalArgumentException) {
            // best effort on unsupported devices
        }
    }
}

@Singleton
class AccelerometerController @Inject constructor(
    @ApplicationContext context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun highRateSamples(): Flow<VibrationSample> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val values = event.values
                trySend(
                    VibrationSample(
                        timestampEpochMillis = System.currentTimeMillis(),
                        x = values.getOrElse(0) { 0f },
                        y = values.getOrElse(1) { 0f },
                        z = values.getOrElse(2) { 0f }
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sensorManager.registerListener(
            listener,
            sensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
