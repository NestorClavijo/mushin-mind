package com.mushind.mind

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mushind.mind.data.preferences.ReminderPreferences
import com.mushind.mind.platform.notifications.PlanningReminderScheduler
import com.mushind.mind.platform.workers.DailyCycleWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltAndroidApp
class MindApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var reminderPreferences: ReminderPreferences
    @Inject lateinit var reminderScheduler: PlanningReminderScheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        DailyCycleWorkScheduler.schedule(this)
        applicationScope.launch {
            reminderScheduler.sync(reminderPreferences.settings.first())
        }
    }
}
