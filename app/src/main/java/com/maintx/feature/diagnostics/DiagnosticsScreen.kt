package com.maintx.feature.diagnostics

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DiagnosticsScreen(
    paddingValues: PaddingValues,
    viewModel: DiagnosticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.onIntent(DiagnosticsIntent.ForegroundChanged(true))
                Lifecycle.Event.ON_STOP -> viewModel.onIntent(DiagnosticsIntent.ForegroundChanged(false))
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ProximityEvents(context = context, onNearChanged = {
        viewModel.onIntent(DiagnosticsIntent.ProximityChanged(it))
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Diagnostics", style = MaterialTheme.typography.headlineSmall)
        Text(uiState.statusMessage)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Stroboscopic Tachometer")
                Text("Pulse: ${"%.1f".format(uiState.pulseFrequencyHz)} Hz")
                Slider(
                    value = uiState.pulseFrequencyHz,
                    onValueChange = { viewModel.onIntent(DiagnosticsIntent.PulseFrequencyChanged(it)) },
                    valueRange = 1f..25f
                )
                Text("Duty cycle: ${(uiState.dutyCycle * 100).toInt()}%")
                Slider(
                    value = uiState.dutyCycle,
                    onValueChange = { viewModel.onIntent(DiagnosticsIntent.DutyCycleChanged(it)) },
                    valueRange = 0.1f..0.9f
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Hard stop")
                    Switch(
                        checked = uiState.hardStopEnabled,
                        onCheckedChange = { viewModel.onIntent(DiagnosticsIntent.HardStopChanged(it)) }
                    )
                }
                Button(onClick = {
                    viewModel.onIntent(DiagnosticsIntent.SetStrobeRunning(!uiState.strobeRunning))
                }) {
                    Text(if (uiState.strobeRunning) "Stop strobe" else "Start strobe")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Accelerometer vibration logger")
                Text("Latest magnitude: ${"%.2f".format(uiState.latestMagnitude)} m/s²")
                VibrationGraph(points = uiState.graphPoints)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Greasy-hand mode")
                Text("Proximity: ${if (uiState.proximityNear) "Near" else "Far"}")
                Text("Active panel index: ${uiState.activePanelIndex}")
                Text("Near toggles flashlight + next panel, far goes previous. Debounced at 500ms.")
            }
        }
    }
}

@Composable
private fun VibrationGraph(points: List<VibrationPoint>) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        if (points.size < 2) return@Canvas
        val maxMagnitude = (points.maxOfOrNull { it.magnitude } ?: 1f).coerceAtLeast(1f)
        val stepX = size.width / (points.size - 1)
        for (index in 1 until points.size) {
            val prev = points[index - 1]
            val current = points[index]
            val start = Offset(
                x = (index - 1) * stepX,
                y = size.height - ((prev.magnitude / maxMagnitude) * size.height)
            )
            val end = Offset(
                x = index * stepX,
                y = size.height - ((current.magnitude / maxMagnitude) * size.height)
            )
            drawLine(
                color = Color.Cyan,
                start = start,
                end = end,
                strokeWidth = 3f
            )
        }
    }
}

@Composable
private fun ProximityEvents(
    context: Context,
    onNearChanged: (Boolean) -> Unit
) {
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        if (sensor == null) {
            onDispose { }
        } else {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val isNear = event.values.firstOrNull()?.let { value ->
                        value < sensor.maximumRange
                    } ?: false
                    onNearChanged(isNear)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)

            onDispose {
                sensorManager.unregisterListener(listener)
            }
        }
    }
}
