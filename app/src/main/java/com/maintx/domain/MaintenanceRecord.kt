package com.maintx.domain

data class MaintenanceRecord(
    val id: Long,
    val vehicleName: String,
    val serviceSummary: String,
    val odometer: Int
)
