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
import com.mushind.mind.data.repository.AndroidAppCatalogRepository
import com.mushind.mind.data.repository.RoomAppRulesRepository
import com.mushind.mind.data.repository.RoomDailyCycleRepository
import com.mushind.mind.data.repository.RoomDailyPlanRepository
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.repository.DailyCycleRepository
import com.mushind.mind.domain.repository.DailyPlanRepository
import com.mushind.mind.domain.repository.AppCatalogRepository
import com.mushind.mind.domain.repository.AppRulesRepository
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
}

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MindDatabase =
        Room.databaseBuilder(context, MindDatabase::class.java, "mind.db")
            .addMigrations(MIGRATION_1_2)
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
}
