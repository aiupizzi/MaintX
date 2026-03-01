package com.maintx.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_records ORDER BY id DESC")
    suspend fun getAll(): List<MaintenanceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MaintenanceEntity)
}
