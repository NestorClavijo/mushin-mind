package com.mushind.mind.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mushind.mind.data.local.dao.DailyCycleDao
import com.mushind.mind.data.local.dao.DailyPlanDao
import com.mushind.mind.data.local.dao.TaskDao
import com.mushind.mind.data.local.dao.UnlockSessionDao
import com.mushind.mind.data.local.dao.AppRulesDao
import com.mushind.mind.data.local.entity.DailyCycleStateEntity
import com.mushind.mind.data.local.entity.DailyPlanEntity
import com.mushind.mind.data.local.entity.DailySummaryEntity
import com.mushind.mind.data.local.entity.PointTransactionEntity
import com.mushind.mind.data.local.entity.TaskEntity
import com.mushind.mind.data.local.entity.UnlockSessionEntity
import com.mushind.mind.data.local.entity.UserProgressEntity
import com.mushind.mind.data.local.entity.RestrictedAppEntity
import com.mushind.mind.data.local.entity.AppRuleEntity

@Database(
    entities = [
        DailyPlanEntity::class,
        TaskEntity::class,
        PointTransactionEntity::class,
        UserProgressEntity::class,
        DailySummaryEntity::class,
        DailyCycleStateEntity::class,
        UnlockSessionEntity::class,
        RestrictedAppEntity::class,
        AppRuleEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
abstract class MindDatabase : RoomDatabase() {
    abstract fun dailyPlanDao(): DailyPlanDao
    abstract fun dailyCycleDao(): DailyCycleDao
    abstract fun taskDao(): TaskDao
    abstract fun unlockSessionDao(): UnlockSessionDao
    abstract fun appRulesDao(): AppRulesDao
}
