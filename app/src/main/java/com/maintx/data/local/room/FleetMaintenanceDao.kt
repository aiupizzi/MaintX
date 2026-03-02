package com.maintx.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface FleetMaintenanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVehicleProfile(profile: VehicleProfileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServiceLog(log: ServiceLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPart(part: PartsBinEntity): Long

    @Transaction
    @Query("SELECT * FROM vehicle_profiles WHERE id = :vehicleId")
    suspend fun getVehicleProfileWithLogs(vehicleId: Long): VehicleProfileWithLogs?

    @Transaction
    @Query("SELECT * FROM vehicle_profiles WHERE vin = :vin AND type = :vehicleType LIMIT 1")
    suspend fun getVehicleProfileWithLogsByVinAndType(
        vin: String,
        vehicleType: VehicleType
    ): VehicleProfileWithLogs?

    @Query(
        """
        UPDATE parts_bin
        SET quantity = quantity - :quantityUsed
        WHERE part_number = :partNumber
          AND quantity >= :quantityUsed
        """
    )
    suspend fun decrementPartQuantity(partNumber: String, quantityUsed: Int): Int

    @Query("SELECT quantity FROM parts_bin WHERE part_number = :partNumber LIMIT 1")
    suspend fun getPartQuantity(partNumber: String): Int?

    @Transaction
    suspend fun recordInventoryUsage(partNumber: String, quantityUsed: Int): Boolean {
        val updatedRows = decrementPartQuantity(partNumber = partNumber, quantityUsed = quantityUsed)
        return updatedRows > 0
    }
}
