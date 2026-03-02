package com.maintx.feature.diagnostics

import com.maintx.data.local.room.DiagnosticsDao
import com.maintx.data.local.room.VibrationSampleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosticsRepository @Inject constructor(
    private val diagnosticsDao: DiagnosticsDao
) {
    suspend fun persistSample(sample: VibrationSample) {
        diagnosticsDao.insertSample(
            VibrationSampleEntity(
                timestampEpochMillis = sample.timestampEpochMillis,
                x = sample.x,
                y = sample.y,
                z = sample.z
            )
        )
    }

    fun observeRecentSamples(limit: Int): Flow<List<VibrationSample>> {
        return diagnosticsDao.observeLatestSamples(limit).map { items ->
            items.asReversed().map {
                VibrationSample(
                    timestampEpochMillis = it.timestampEpochMillis,
                    x = it.x,
                    y = it.y,
                    z = it.z
                )
            }
        }
    }
}
