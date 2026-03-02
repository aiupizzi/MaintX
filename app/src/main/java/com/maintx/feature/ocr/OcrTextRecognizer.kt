package com.maintx.feature.ocr

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import javax.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class OcrTextRecognizer @Inject constructor(
    private val textRecognizer: TextRecognizer
) {
    suspend fun recognize(image: InputImage): OcrRecognitionResult {
        val result = suspendCancellableCoroutine<String> { continuation ->
            textRecognizer.process(image)
                .addOnSuccessListener { continuation.resume(it.text) }
                .addOnFailureListener { continuation.resumeWithException(it) }
        }
        return parseOcrText(result)
    }
}
