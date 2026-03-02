package com.maintx.feature.ocr

private val vinRegex = Regex("[A-HJ-NPR-Z0-9]{17}")
private val tireRegex = Regex("""(\d{3}/\d{2}R\d{2})\s*(\d{2,3})([A-Z])""")

fun normalizeVin(raw: String): String {
    val compact = raw.uppercase().filter { it.isLetterOrDigit() }
    val normalized = compact
        .replace('I', '1')
        .replace('O', '0')
        .replace('Q', '0')
    return normalized.take(17)
}

fun parseOcrText(text: String): OcrRecognitionResult {
    val sanitized = text.uppercase().replace("\n", " ")
    val vinCandidate = vinRegex.find(normalizeVin(sanitized))?.value
    val tireMatch = tireRegex.find(sanitized)

    return OcrRecognitionResult(
        rawText = text,
        vin = vinCandidate,
        tireSize = tireMatch?.groupValues?.getOrNull(1),
        tireLoadIndex = tireMatch?.groupValues?.getOrNull(2),
        tireSpeedRating = tireMatch?.groupValues?.getOrNull(3)
    )
}
