package com.maintx.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MaintenanceEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MaintXDatabase : RoomDatabase() {
    abstract fun maintenanceDao(): MaintenanceDao
}
