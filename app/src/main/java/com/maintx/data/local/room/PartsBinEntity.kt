package com.maintx.data.local.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "parts_bin",
    indices = [Index(value = ["part_number"], unique = true)]
)
data class PartsBinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val part_number: String,
    val description: String,
    val quantity: Int,
    val minQuantity: Int,
    val compatibleVehicleTypes: List<VehicleType>,
    val storageLocation: String
)
