package com.maintx.data.local.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object RoomMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `vehicle_profiles` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `type` TEXT NOT NULL,
                    `make` TEXT NOT NULL,
                    `model` TEXT NOT NULL,
                    `year` INTEGER NOT NULL,
                    `vin` TEXT NOT NULL,
                    `odometer` INTEGER NOT NULL,
                    `customSpecsJson` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `service_logs` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `vehicle_id` INTEGER NOT NULL,
                    `timestampEpochMillis` INTEGER NOT NULL,
                    `serviceType` TEXT NOT NULL,
                    `notes` TEXT NOT NULL,
                    `intervalMileage` INTEGER,
                    `intervalDays` INTEGER,
                    `predictedNextServiceEpochMillis` INTEGER,
                    `predictedNextServiceOdometer` INTEGER,
                    FOREIGN KEY(`vehicle_id`) REFERENCES `vehicle_profiles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `parts_bin` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `part_number` TEXT NOT NULL,
                    `description` TEXT NOT NULL,
                    `quantity` INTEGER NOT NULL,
                    `minQuantity` INTEGER NOT NULL,
                    `compatibleVehicleTypes` TEXT NOT NULL,
                    `storageLocation` TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_vehicle_profiles_vin` ON `vehicle_profiles` (`vin`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_vehicle_profiles_type` ON `vehicle_profiles` (`type`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_service_logs_vehicle_id` ON `service_logs` (`vehicle_id`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_parts_bin_part_number` ON `parts_bin` (`part_number`)")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `vibration_samples` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `timestampEpochMillis` INTEGER NOT NULL,
                    `x` REAL NOT NULL,
                    `y` REAL NOT NULL,
                    `z` REAL NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_vibration_samples_timestampEpochMillis` ON `vibration_samples` (`timestampEpochMillis`)"
            )
        }
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
