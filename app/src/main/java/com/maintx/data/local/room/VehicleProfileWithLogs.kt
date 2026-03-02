package com.maintx.data.local.room

import androidx.room.Embedded
import androidx.room.Relation

data class VehicleProfileWithLogs(
    @Embedded val vehicleProfile: VehicleProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "vehicle_id"
    )
    val serviceLogs: List<ServiceLogEntity>
)
