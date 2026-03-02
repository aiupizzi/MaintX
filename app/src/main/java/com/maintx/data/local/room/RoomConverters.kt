package com.maintx.data.local.room

import androidx.room.TypeConverter

class RoomConverters {
    @TypeConverter
    fun fromVehicleType(value: VehicleType): String = value.name

    @TypeConverter
    fun toVehicleType(value: String): VehicleType = VehicleType.valueOf(value)

    @TypeConverter
    fun fromVehicleTypeList(value: List<VehicleType>): String = value.joinToString(",") { it.name }

    @TypeConverter
    fun toVehicleTypeList(value: String): List<VehicleType> {
        if (value.isBlank()) return emptyList()
        return value.split(',').map(VehicleType::valueOf)
    }
}
