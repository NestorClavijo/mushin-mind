package com.mushind.mind.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `restricted_apps` (
                `packageName` TEXT NOT NULL,
                `displayName` TEXT NOT NULL,
                `isEnabled` INTEGER NOT NULL,
                `isCritical` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`packageName`)
            )""".trimIndent(),
        )
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `app_rules` (
                `packageName` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `costPoints` INTEGER NOT NULL,
                `durationMinutes` INTEGER,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`packageName`),
                FOREIGN KEY(`packageName`) REFERENCES `restricted_apps`(`packageName`) ON UPDATE NO ACTION ON DELETE CASCADE
            )""".trimIndent(),
        )
    }
}
