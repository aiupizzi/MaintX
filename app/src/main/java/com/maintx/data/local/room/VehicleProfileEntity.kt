package com.maintx.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehicle_profiles",
    indices = [
        Index(value = ["vin"], unique = true),
        Index(value = ["type"])
    ]
)
data class VehicleProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: VehicleType,
    val make: String,
    val model: String,
    val year: Int,
    val vin: String,
    val odometer: Int,
    val customSpecsJson: String
)
