package com.maintx.di

import android.content.Context
import androidx.room.Room
import com.maintx.data.MaintXDatabase
import com.maintx.data.MaintenanceDao
import com.maintx.data.local.room.FleetMaintenanceDao
import com.maintx.data.local.room.RoomMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MaintXDatabase {
        return Room.databaseBuilder(
            context,
            MaintXDatabase::class.java,
            "maintx.db"
        )
            .addMigrations(*RoomMigrations.ALL)
            .build()
    }

    @Provides
    fun provideMaintenanceDao(database: MaintXDatabase): MaintenanceDao = database.maintenanceDao()

    @Provides
    fun provideFleetMaintenanceDao(database: MaintXDatabase): FleetMaintenanceDao = database.fleetMaintenanceDao()
}
