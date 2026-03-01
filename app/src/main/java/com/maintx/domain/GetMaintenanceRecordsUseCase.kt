package com.maintx.domain

import com.maintx.data.MaintenanceRepository
import javax.inject.Inject

class GetMaintenanceRecordsUseCase @Inject constructor(
    private val repository: MaintenanceRepository
) {
    suspend operator fun invoke(): List<MaintenanceRecord> = repository.getMaintenanceRecords()
}
