package com.maintx.feature.ocr

import com.maintx.data.local.room.VehicleType

data class OcrUiState(
    val cameraPermissionGranted: Boolean = false,
    val isProcessing: Boolean = false,
    val rawRecognizedText: String = "",
    val vin: String = "",
    val tireSize: String = "",
    val tireLoadIndex: String = "",
    val tireSpeedRating: String = "",
    val make: String = "",
    val model: String = "",
    val year: String = "",
    val odometer: String = "",
    val vehicleType: VehicleType = VehicleType.CAR,
    val serviceNotes: String = "",
    val statusMessage: String = "Point camera at VIN/tire label and capture an image.",
    val showConfirmation: Boolean = false,
    val captureRequestedAtMillis: Long = 0L
)

data class OcrRecognitionResult(
    val rawText: String,
    val vin: String?,
    val tireSize: String?,
    val tireLoadIndex: String?,
    val tireSpeedRating: String?
)
