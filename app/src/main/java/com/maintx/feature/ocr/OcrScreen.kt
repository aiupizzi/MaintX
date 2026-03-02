package com.maintx.feature.ocr

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.mlkit.vision.common.InputImage
import com.maintx.data.local.room.VehicleType

@Composable
fun OcrScreen(
    paddingValues: PaddingValues,
    viewModel: OcrViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { viewModel.onCameraPermissionChanged(it) }
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onCameraPermissionChanged(granted)
        if (!granted) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("OCR Intake", style = MaterialTheme.typography.headlineSmall)
        Text(uiState.statusMessage)

        if (!uiState.cameraPermissionGranted) {
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant camera permission")
            }
        } else {
            CameraPreview(
                context = context,
                lifecycleOwner = lifecycleOwner,
                onImageCaptureReady = { imageCapture = it }
            )

            Button(
                onClick = {
                    viewModel.requestCapture()
                    imageCapture?.let { capture ->
                        capture.takePicture(
                            mainExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                                    val mediaImage = image.image
                                    if (mediaImage != null) {
                                        val input = InputImage.fromMediaImage(
                                            mediaImage,
                                            image.imageInfo.rotationDegrees
                                        )
                                        viewModel.onImageCaptured(input)
                                    }
                                    image.close()
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    viewModel.onCaptureError(exception.message ?: "Unknown camera error")
                                }
                            }
                        )
                    }
                },
                enabled = !uiState.isProcessing && imageCapture != null
            ) {
                Text("Capture image")
            }
        }

        if (uiState.isProcessing) {
            CircularProgressIndicator()
        }

        if (uiState.showConfirmation) {
            ConfirmationCard(
                state = uiState,
                onFieldUpdate = viewModel::updateField,
                onSave = viewModel::saveConfirmedData
            )
        }
    }
}

@Composable
private fun CameraPreview(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        val imageCapture = ImageCapture.Builder().build()

        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageCapture
            )
            onImageCaptureReady(imageCapture)
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { cameraProviderFuture.get().unbindAll() }
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}

@Composable
private fun ConfirmationCard(
    state: OcrUiState,
    onFieldUpdate: (
        vin: String,
        tireSize: String,
        tireLoadIndex: String,
        tireSpeedRating: String,
        make: String,
        model: String,
        year: String,
        odometer: String,
        serviceNotes: String,
        vehicleType: VehicleType
    ) -> Unit,
    onSave: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Confirm / Edit OCR Data")
            OutlinedTextField(value = state.vin, onValueChange = {
                onFieldUpdate(
                    it,
                    state.tireSize,
                    state.tireLoadIndex,
                    state.tireSpeedRating,
                    state.make,
                    state.model,
                    state.year,
                    state.odometer,
                    state.serviceNotes,
                    state.vehicleType
                )
            }, label = { Text("VIN") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.tireSize, onValueChange = {
                onFieldUpdate(
                    state.vin,
                    it,
                    state.tireLoadIndex,
                    state.tireSpeedRating,
                    state.make,
                    state.model,
                    state.year,
                    state.odometer,
                    state.serviceNotes,
                    state.vehicleType
                )
            }, label = { Text("Tire size (e.g. 225/65R17)") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = state.tireLoadIndex,
                    onValueChange = {
                        onFieldUpdate(
                            state.vin,
                            state.tireSize,
                            it,
                            state.tireSpeedRating,
                            state.make,
                            state.model,
                            state.year,
                            state.odometer,
                            state.serviceNotes,
                            state.vehicleType
                        )
                    },
                    label = { Text("Load") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.tireSpeedRating,
                    onValueChange = {
                        onFieldUpdate(
                            state.vin,
                            state.tireSize,
                            state.tireLoadIndex,
                            it,
                            state.make,
                            state.model,
                            state.year,
                            state.odometer,
                            state.serviceNotes,
                            state.vehicleType
                        )
                    },
                    label = { Text("Speed") },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(value = state.make, onValueChange = {
                onFieldUpdate(
                    state.vin,
                    state.tireSize,
                    state.tireLoadIndex,
                    state.tireSpeedRating,
                    it,
                    state.model,
                    state.year,
                    state.odometer,
                    state.serviceNotes,
                    state.vehicleType
                )
            }, label = { Text("Make") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = state.model, onValueChange = {
                onFieldUpdate(
                    state.vin,
                    state.tireSize,
                    state.tireLoadIndex,
                    state.tireSpeedRating,
                    state.make,
                    it,
                    state.year,
                    state.odometer,
                    state.serviceNotes,
                    state.vehicleType
                )
            }, label = { Text("Model") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = state.year, onValueChange = {
                    onFieldUpdate(
                        state.vin,
                        state.tireSize,
                        state.tireLoadIndex,
                        state.tireSpeedRating,
                        state.make,
                        state.model,
                        it,
                        state.odometer,
                        state.serviceNotes,
                        state.vehicleType
                    )
                }, label = { Text("Year") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = state.odometer, onValueChange = {
                    onFieldUpdate(
                        state.vin,
                        state.tireSize,
                        state.tireLoadIndex,
                        state.tireSpeedRating,
                        state.make,
                        state.model,
                        state.year,
                        it,
                        state.serviceNotes,
                        state.vehicleType
                    )
                }, label = { Text("Odometer") }, modifier = Modifier.weight(1f))
            }
            OutlinedTextField(value = state.serviceNotes, onValueChange = {
                onFieldUpdate(
                    state.vin,
                    state.tireSize,
                    state.tireLoadIndex,
                    state.tireSpeedRating,
                    state.make,
                    state.model,
                    state.year,
                    state.odometer,
                    it,
                    state.vehicleType
                )
            }, label = { Text("Service log notes") }, modifier = Modifier.fillMaxWidth())
            Text("Raw OCR Text")
            Text(state.rawRecognizedText)
            Button(onClick = onSave) {
                Text("Save to Room")
            }
        }
    }
}
