package com.maintx.feature.ocr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.maintx.data.local.room.VehicleType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val ocrTextRecognizer: OcrTextRecognizer,
    private val ocrRepository: OcrRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    fun onCameraPermissionChanged(granted: Boolean) {
        _uiState.update { it.copy(cameraPermissionGranted = granted) }
    }

    fun requestCapture() {
        _uiState.update { it.copy(captureRequestedAtMillis = System.currentTimeMillis()) }
    }

    fun onImageCaptured(image: InputImage) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, statusMessage = "Running on-device OCR...") }
            runCatching { ocrTextRecognizer.recognize(image) }
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            rawRecognizedText = result.rawText,
                            vin = result.vin.orEmpty(),
                            tireSize = result.tireSize.orEmpty(),
                            tireLoadIndex = result.tireLoadIndex.orEmpty(),
                            tireSpeedRating = result.tireSpeedRating.orEmpty(),
                            showConfirmation = true,
                            statusMessage = "OCR complete. Confirm or edit fields before saving."
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            showConfirmation = false,
                            statusMessage = "OCR failed: ${error.message ?: "Unknown error"}"
                        )
                    }
                }
        }
    }

    fun onCaptureError(message: String) {
        _uiState.update { it.copy(statusMessage = "Capture failed: $message") }
    }

    fun updateField(
        vin: String = uiState.value.vin,
        tireSize: String = uiState.value.tireSize,
        tireLoadIndex: String = uiState.value.tireLoadIndex,
        tireSpeedRating: String = uiState.value.tireSpeedRating,
        make: String = uiState.value.make,
        model: String = uiState.value.model,
        year: String = uiState.value.year,
        odometer: String = uiState.value.odometer,
        serviceNotes: String = uiState.value.serviceNotes,
        vehicleType: VehicleType = uiState.value.vehicleType
    ) {
        _uiState.update {
            it.copy(
                vin = normalizeVin(vin),
                tireSize = tireSize.uppercase(),
                tireLoadIndex = tireLoadIndex,
                tireSpeedRating = tireSpeedRating.uppercase(),
                make = make,
                model = model,
                year = year.filter(Char::isDigit).take(4),
                odometer = odometer.filter(Char::isDigit),
                serviceNotes = serviceNotes,
                vehicleType = vehicleType
            )
        }
    }

    fun saveConfirmedData() {
        val state = uiState.value
        viewModelScope.launch {
            val vin = state.vin
            if (vin.length != 17) {
                _uiState.update { it.copy(statusMessage = "VIN must be 17 characters before saving.") }
                return@launch
            }

            val year = state.year.toIntOrNull() ?: 2000
            val odometer = state.odometer.toIntOrNull() ?: 0

            runCatching {
                ocrRepository.saveRecognizedVehicleData(
                    vin = vin,
                    make = state.make,
                    model = state.model,
                    year = year,
                    odometer = odometer,
                    type = state.vehicleType,
                    tireSize = state.tireSize,
                    tireLoadIndex = state.tireLoadIndex,
                    tireSpeedRating = state.tireSpeedRating,
                    notes = state.serviceNotes
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        showConfirmation = false,
                        statusMessage = "Saved OCR data to vehicle profile and service log."
                    )
                }
            }.onFailure {
                _uiState.update { current ->
                    current.copy(statusMessage = "Save failed: ${it.message ?: "Unknown error"}")
                }
            }
        }
    }
}
