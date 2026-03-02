package com.maintx.feature.ocr

import com.maintx.data.local.room.FleetMaintenanceDao
import com.maintx.data.local.room.ServiceLogEntity
import com.maintx.data.local.room.VehicleProfileEntity
import com.maintx.data.local.room.VehicleType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OcrRepository @Inject constructor(
    private val fleetMaintenanceDao: FleetMaintenanceDao
) {
    suspend fun saveRecognizedVehicleData(
        vin: String,
        make: String,
        model: String,
        year: Int,
        odometer: Int,
        type: VehicleType,
        tireSize: String,
        tireLoadIndex: String,
        tireSpeedRating: String,
        notes: String
    ) {
        val normalizedVin = normalizeVin(vin)
        val existing = fleetMaintenanceDao.getVehicleProfileWithLogsByVinAndType(
            vin = normalizedVin,
            vehicleType = type
        )

        val specs = """
            {
              "tireSize":"$tireSize",
              "tireLoadIndex":"$tireLoadIndex",
              "tireSpeedRating":"$tireSpeedRating"
            }
        """.trimIndent()

        val vehicleId = fleetMaintenanceDao.upsertVehicleProfile(
            VehicleProfileEntity(
                id = existing?.vehicleProfile?.id ?: 0,
                type = type,
                make = make.ifBlank { "Unknown" },
                model = model.ifBlank { "Unknown" },
                year = year,
                vin = normalizedVin,
                odometer = odometer,
                customSpecsJson = specs
            )
        )

        fleetMaintenanceDao.upsertServiceLog(
            ServiceLogEntity(
                vehicle_id = if (existing == null) vehicleId else existing.vehicleProfile.id,
                timestampEpochMillis = System.currentTimeMillis(),
                serviceType = "OCR Intake",
                notes = notes.ifBlank {
                    "Captured via OCR. VIN=$normalizedVin, tire=$tireSize $tireLoadIndex$tireSpeedRating"
                },
                intervalMileage = null,
                intervalDays = null,
                predictedNextServiceEpochMillis = null,
                predictedNextServiceOdometer = null
            )
        )
    }
}
