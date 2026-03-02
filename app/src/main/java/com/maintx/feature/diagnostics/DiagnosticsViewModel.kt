package com.maintx.feature.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val torchController: TorchController,
    private val accelerometerController: AccelerometerController,
    private val diagnosticsRepository: DiagnosticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = _uiState.asStateFlow()

    private var strobeJob: Job? = null
    private var accelerometerJob: Job? = null
    private var proximityJob: Job? = null
    private var lastProximityAt = 0L

    init {
        observeRecentSamples()
        startAccelerometerLogging()
    }

    fun onIntent(intent: DiagnosticsIntent) {
        when (intent) {
            is DiagnosticsIntent.PulseFrequencyChanged -> {
                _uiState.update { it.copy(pulseFrequencyHz = intent.value.coerceIn(1f, 25f)) }
                restartStrobeIfNeeded()
            }

            is DiagnosticsIntent.DutyCycleChanged -> {
                _uiState.update { it.copy(dutyCycle = intent.value.coerceIn(0.1f, 0.9f)) }
                restartStrobeIfNeeded()
            }

            is DiagnosticsIntent.HardStopChanged -> {
                _uiState.update { it.copy(hardStopEnabled = intent.enabled) }
                if (intent.enabled) {
                    stopStrobe("Hard stop engaged")
                }
            }

            is DiagnosticsIntent.SetStrobeRunning -> {
                if (intent.running) {
                    startStrobe()
                } else {
                    stopStrobe("Strobe stopped")
                }
            }

            is DiagnosticsIntent.ProximityChanged -> handleProximity(intent.isNear)
            DiagnosticsIntent.NextPanel -> stepPanel(1)
            DiagnosticsIntent.PreviousPanel -> stepPanel(-1)
            is DiagnosticsIntent.ForegroundChanged -> handleForegroundChange(intent.inForeground)
        }
    }

    private fun handleForegroundChange(inForeground: Boolean) {
        _uiState.update { it.copy(isForeground = inForeground) }
        if (!inForeground) {
            stopStrobe("Paused while app is backgrounded")
            stopAccelerometerLogging()
        } else {
            startAccelerometerLogging()
        }
    }

    private fun handleProximity(isNear: Boolean) {
        val now = System.currentTimeMillis()
        if (now - lastProximityAt < 500L) return
        lastProximityAt = now

        _uiState.update { it.copy(proximityNear = isNear) }

        if (isNear) {
            onIntent(DiagnosticsIntent.SetStrobeRunning(!_uiState.value.strobeRunning))
            onIntent(DiagnosticsIntent.NextPanel)
        } else {
            onIntent(DiagnosticsIntent.PreviousPanel)
        }
    }

    private fun stepPanel(delta: Int) {
        _uiState.update { state ->
            val next = (state.activePanelIndex + delta).mod(3)
            state.copy(activePanelIndex = next)
        }
    }

    private fun startStrobe() {
        val state = _uiState.value
        if (state.hardStopEnabled || !state.isForeground) {
            _uiState.update { it.copy(statusMessage = "Strobe blocked by safety state") }
            return
        }
        if (strobeJob?.isActive == true) return

        _uiState.update { it.copy(strobeRunning = true, statusMessage = "Strobe running") }
        strobeJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val cycleMillis = max(40L, (1_000f / _uiState.value.pulseFrequencyHz).toLong())
                val onMillis = (cycleMillis * _uiState.value.dutyCycle).toLong().coerceAtLeast(10L)
                val offMillis = (cycleMillis - onMillis).coerceAtLeast(10L)

                withContext(Dispatchers.IO) { torchController.setTorch(true) }
                delay(onMillis)
                withContext(Dispatchers.IO) { torchController.setTorch(false) }
                delay(offMillis)
            }
        }
    }

    private fun restartStrobeIfNeeded() {
        if (_uiState.value.strobeRunning) {
            stopStrobe("Updating strobe profile")
            startStrobe()
        }
    }

    private fun stopStrobe(reason: String) {
        strobeJob?.cancel()
        strobeJob = null
        torchController.setTorch(false)
        _uiState.update { it.copy(strobeRunning = false, statusMessage = reason) }
    }

    private fun startAccelerometerLogging() {
        if (accelerometerJob?.isActive == true) return
        accelerometerJob = viewModelScope.launch(Dispatchers.Default) {
            accelerometerController.highRateSamples().collectLatest { sample ->
                withContext(Dispatchers.IO) {
                    diagnosticsRepository.persistSample(sample)
                }
            }
        }
    }

    private fun stopAccelerometerLogging() {
        accelerometerJob?.cancel()
        accelerometerJob = null
    }

    private fun observeRecentSamples() {
        proximityJob = viewModelScope.launch(Dispatchers.Default) {
            diagnosticsRepository.observeRecentSamples(limit = 180).collectLatest { samples ->
                val points = samples.takeLast(120).map {
                    VibrationPoint(it.timestampEpochMillis, it.magnitude)
                }
                val latestMagnitude = points.lastOrNull()?.magnitude ?: 0f
                _uiState.update {
                    it.copy(
                        latestMagnitude = latestMagnitude,
                        graphPoints = points
                    )
                }
            }
        }
    }

    override fun onCleared() {
        stopStrobe("Diagnostics closed")
        stopAccelerometerLogging()
        proximityJob?.cancel()
        super.onCleared()
    }
}
