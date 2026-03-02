package com.maintx.ui.navigation

enum class MaintXDestination(val route: String, val title: String) {
    Vehicle("vehicle", "Vehicle"),
    ServiceLog("service_log", "Service Log"),
    PartsBin("parts_bin", "Parts Bin"),
    Ocr("ocr", "OCR"),
    Diagnostics("diagnostics", "Diagnostics"),
    ImportExport("import_export", "Import / Export")
}
