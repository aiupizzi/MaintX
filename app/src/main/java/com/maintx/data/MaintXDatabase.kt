package com.maintx.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.maintx.data.local.room.FleetMaintenanceDao
import com.maintx.data.local.room.PartsBinEntity
import com.maintx.data.local.room.RoomConverters
import com.maintx.data.local.room.ServiceLogEntity
import com.maintx.data.local.room.VehicleProfileEntity

@Database(
    entities = [
        MaintenanceEntity::class,
        VehicleProfileEntity::class,
        ServiceLogEntity::class,
        PartsBinEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class MaintXDatabase : RoomDatabase() {
    abstract fun maintenanceDao(): MaintenanceDao
    abstract fun fleetMaintenanceDao(): FleetMaintenanceDao
}
