package com.maintx.data.local.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vibration_samples",
    indices = [Index(value = ["timestampEpochMillis"])]
)
data class VibrationSampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "timestampEpochMillis")
    val timestampEpochMillis: Long,
    val x: Float,
    val y: Float,
    val z: Float
)
