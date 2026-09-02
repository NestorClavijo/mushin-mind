package com.mushind.mind.platform.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mushind.mind.R
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.data.preferences.ReminderSettings
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@HiltWorker
class PlanningReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return Result.success()

        createChannel()
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(applicationContext.getString(R.string.planning_reminder_title))
            .setContentText(applicationContext.getString(R.string.planning_reminder_body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
        return Result.success()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            applicationContext.getString(R.string.planning_reminder_channel),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        applicationContext.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "planning_reminders"
        const val NOTIFICATION_ID = 1001
    }
}

@Singleton
class PlanningReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val clock: ClockProvider,
) {
    fun sync(settings: ReminderSettings) {
        val workManager = WorkManager.getInstance(context)
        if (!settings.enabled) {
            workManager.cancelUniqueWork(WORK_NAME)
            return
        }

        val request = planningReminderRequest(settings, clock)
        workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private companion object {
        const val WORK_NAME = "planning-reminder"
    }
}

internal fun planningReminderRequest(
    settings: ReminderSettings,
    clock: ClockProvider,
): PeriodicWorkRequest {
    require(settings.enabled)
    val now = ZonedDateTime.ofInstant(clock.now(), clock.zoneId())
    var next = now.toLocalDate().atTime(settings.hour, settings.minute).atZone(clock.zoneId())
    if (!next.isAfter(now)) next = next.plusDays(1)
    return PeriodicWorkRequestBuilder<PlanningReminderWorker>(24, TimeUnit.HOURS)
        .setInitialDelay(Duration.between(now, next))
        .build()
}
