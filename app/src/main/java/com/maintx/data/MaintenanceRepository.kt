package com.maintx.data

import com.maintx.domain.MaintenanceRecord
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MaintenanceRepository @Inject constructor(
    private val dao: MaintenanceDao
) {
    suspend fun getMaintenanceRecords(): List<MaintenanceRecord> {
        return dao.getAll().map {
            MaintenanceRecord(
                id = it.id,
                vehicleName = it.vehicleName,
                serviceSummary = it.serviceSummary,
                odometer = it.odometer
            )
        }
    }
}
