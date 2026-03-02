package com.maintx.feature.diagnostics

import kotlin.math.sqrt

data class VibrationPoint(
    val timestampEpochMillis: Long,
    val magnitude: Float
)

data class DiagnosticsUiState(
    val isForeground: Boolean = true,
    val pulseFrequencyHz: Float = 8f,
    val dutyCycle: Float = 0.5f,
    val hardStopEnabled: Boolean = false,
    val strobeRunning: Boolean = false,
    val proximityNear: Boolean = false,
    val activePanelIndex: Int = 0,
    val latestMagnitude: Float = 0f,
    val graphPoints: List<VibrationPoint> = emptyList(),
    val statusMessage: String = "Ready"
)

sealed interface DiagnosticsIntent {
    data class PulseFrequencyChanged(val value: Float) : DiagnosticsIntent
    data class DutyCycleChanged(val value: Float) : DiagnosticsIntent
    data class HardStopChanged(val enabled: Boolean) : DiagnosticsIntent
    data class SetStrobeRunning(val running: Boolean) : DiagnosticsIntent
    data class ProximityChanged(val isNear: Boolean) : DiagnosticsIntent
    data object NextPanel : DiagnosticsIntent
    data object PreviousPanel : DiagnosticsIntent
    data class ForegroundChanged(val inForeground: Boolean) : DiagnosticsIntent
}

data class VibrationSample(
    val timestampEpochMillis: Long,
    val x: Float,
    val y: Float,
    val z: Float
) {
    val magnitude: Float
        get() = sqrt((x * x) + (y * y) + (z * z))
}
