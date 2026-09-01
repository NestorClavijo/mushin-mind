package com.mushind.mind.di

import android.content.Context
import androidx.room.Room
import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.core.common.UuidProvider
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.core.time.SystemClockProvider
import com.mushind.mind.data.local.dao.DailyPlanDao
import com.mushind.mind.data.local.dao.AppRulesDao
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.db.MIGRATION_1_2
import com.mushind.mind.data.local.db.MIGRATION_2_3
import com.mushind.mind.data.local.db.MIGRATION_3_4
import com.mushind.mind.data.local.db.MIGRATION_4_5
import com.mushind.mind.data.repository.AndroidAppCatalogRepository
import com.mushind.mind.data.repository.RoomAppRulesRepository
import com.mushind.mind.data.repository.RoomDailyCycleRepository
import com.mushind.mind.data.repository.RoomDailyPlanRepository
import com.mushind.mind.data.repository.RoomUnlockSessionRepository
import com.mushind.mind.data.repository.RoomProtectedRuleChangeRepository
import com.mushind.mind.data.repository.RoomEmergencyUnlockRepository
import com.mushind.mind.data.preferences.EmergencyPreferences
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.repository.DailyCycleRepository
import com.mushind.mind.domain.repository.DailyPlanRepository
import com.mushind.mind.domain.repository.AppCatalogRepository
import com.mushind.mind.domain.repository.AppRulesRepository
import com.mushind.mind.domain.repository.UnlockSessionRepository
import com.mushind.mind.domain.repository.ProtectedRuleChangeRepository
import com.mushind.mind.domain.repository.EmergencyPolicyRepository
import com.mushind.mind.domain.repository.EmergencyUnlockRepository
import com.mushind.mind.domain.usecase.ChallengePolicy
import com.mushind.mind.BuildConfig
import java.time.Duration
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindDailyPlanRepository(implementation: RoomDailyPlanRepository): DailyPlanRepository

    @Binds
    @Singleton
    abstract fun bindDailyCycleRepository(implementation: RoomDailyCycleRepository): DailyCycleRepository

    @Binds
    @Singleton
    abstract fun bindAppCatalogRepository(implementation: AndroidAppCatalogRepository): AppCatalogRepository

    @Binds
    @Singleton
    abstract fun bindAppRulesRepository(implementation: RoomAppRulesRepository): AppRulesRepository

    @Binds
    @Singleton
    abstract fun bindUnlockSessionRepository(
        implementation: RoomUnlockSessionRepository,
    ): UnlockSessionRepository

    @Binds
    @Singleton
    abstract fun bindProtectedRuleChangeRepository(
        implementation: RoomProtectedRuleChangeRepository,
    ): ProtectedRuleChangeRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyPolicyRepository(implementation: EmergencyPreferences): EmergencyPolicyRepository

    @Binds
    @Singleton
    abstract fun bindEmergencyUnlockRepository(
        implementation: RoomEmergencyUnlockRepository,
    ): EmergencyUnlockRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MindDatabase =
        Room.databaseBuilder(context, MindDatabase::class.java, "mind.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()

    @Provides
    fun provideDailyPlanDao(database: MindDatabase): DailyPlanDao = database.dailyPlanDao()

    @Provides
    fun provideAppRulesDao(database: MindDatabase): AppRulesDao = database.appRulesDao()

    @Provides
    @Singleton
    fun provideClock(): ClockProvider = SystemClockProvider()

    @Provides
    @Singleton
    fun provideIdProvider(): IdProvider = UuidProvider()

    @Provides
    @Singleton
    fun provideLogicalDayResolver(): LogicalDayResolver = LogicalDayResolver()

    @Provides
    @Singleton
    fun provideChallengePolicy(): ChallengePolicy = if (BuildConfig.DEBUG) {
        ChallengePolicy(requiredQuestions = 3, minimumDuration = Duration.ofSeconds(2))
    } else {
        ChallengePolicy(requiredQuestions = 12, minimumDuration = Duration.ofMinutes(3))
    }
}
