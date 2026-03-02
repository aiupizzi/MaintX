package com.maintx.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSample(sample: VibrationSampleEntity)

    @Query(
        """
        SELECT * FROM vibration_samples
        ORDER BY timestampEpochMillis DESC
        LIMIT :limit
        """
    )
    fun observeLatestSamples(limit: Int): Flow<List<VibrationSampleEntity>>
}
