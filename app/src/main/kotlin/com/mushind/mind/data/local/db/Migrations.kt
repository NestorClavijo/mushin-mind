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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE `unlock_sessions` ADD COLUMN `ruleType` TEXT NOT NULL DEFAULT 'TEMPORARY_SESSION'",
        )
        db.execSQL(
            "UPDATE `unlock_sessions` SET `ruleType` = 'UNTIL_END_OF_DAY' WHERE `type` = 'UNTIL_END_OF_DAY'",
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `challenge_attempts` (
                `id` TEXT NOT NULL, `packageName` TEXT NOT NULL, `proposedEnabled` INTEGER NOT NULL,
                `proposedRuleType` TEXT, `proposedCostPoints` INTEGER, `proposedDurationMinutes` INTEGER,
                `effectiveDay` TEXT NOT NULL, `requiredQuestions` INTEGER NOT NULL,
                `answeredQuestions` INTEGER NOT NULL, `mistakes` INTEGER NOT NULL,
                `startedAt` INTEGER NOT NULL, `minimumCompletesAt` INTEGER NOT NULL,
                `completedAt` INTEGER, `status` TEXT NOT NULL, PRIMARY KEY(`id`)
            )""".trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_challenge_attempts_packageName` ON `challenge_attempts` (`packageName`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_challenge_attempts_status` ON `challenge_attempts` (`status`)")
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `pending_rule_changes` (
                `id` TEXT NOT NULL, `challengeAttemptId` TEXT NOT NULL, `packageName` TEXT NOT NULL, `proposedEnabled` INTEGER NOT NULL,
                `proposedRuleType` TEXT, `proposedCostPoints` INTEGER, `proposedDurationMinutes` INTEGER,
                `requestedAt` INTEGER NOT NULL, `effectiveDay` TEXT NOT NULL, `status` TEXT NOT NULL,
                `appliedAt` INTEGER, `cancelledAt` INTEGER, PRIMARY KEY(`id`)
            )""".trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_rule_changes_packageName` ON `pending_rule_changes` (`packageName`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_rule_changes_status` ON `pending_rule_changes` (`status`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_rule_changes_effectiveDay` ON `pending_rule_changes` (`effectiveDay`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_pending_rule_changes_challengeAttemptId` ON `pending_rule_changes` (`challengeAttemptId`)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `emergency_unlocks` (
                `id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `packageName` TEXT NOT NULL,
                `reason` TEXT, `durationMinutes` INTEGER NOT NULL,
                `configuredPenaltyPoints` INTEGER NOT NULL, `appliedPenaltyPoints` INTEGER NOT NULL,
                `balanceBefore` INTEGER NOT NULL, `balanceAfter` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`)
            )""".trimIndent(),
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_emergency_unlocks_sessionId` ON `emergency_unlocks` (`sessionId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_emergency_unlocks_packageName` ON `emergency_unlocks` (`packageName`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_emergency_unlocks_createdAt` ON `emergency_unlocks` (`createdAt`)")
    }
}
