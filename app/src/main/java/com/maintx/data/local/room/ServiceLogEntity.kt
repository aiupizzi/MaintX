package com.maintx.data.local.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "service_logs",
    foreignKeys = [
        ForeignKey(
            entity = VehicleProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicle_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["vehicle_id"])]
)
data class ServiceLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicle_id: Long,
    val timestampEpochMillis: Long,
    val serviceType: String,
    val notes: String,
    val intervalMileage: Int?,
    val intervalDays: Int?,
    val predictedNextServiceEpochMillis: Long?,
    val predictedNextServiceOdometer: Int?
)
