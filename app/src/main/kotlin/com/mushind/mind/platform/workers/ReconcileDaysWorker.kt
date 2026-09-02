package com.mushind.mind.platform.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mushind.mind.domain.usecase.ReconcileDays
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException

@HiltWorker
class ReconcileDaysWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reconcileDays: ReconcileDays,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        reconcileDays()
        Result.success()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        Result.retry()
    }
}

internal const val RECONCILE_STARTUP_TAG = "daily-cycle-startup"
internal const val RECONCILE_PERIODIC_TAG = "daily-cycle-periodic"

internal fun startupReconcileRequest() = OneTimeWorkRequestBuilder<ReconcileDaysWorker>()
    .addTag(RECONCILE_STARTUP_TAG)
    .build()

internal fun periodicReconcileRequest() = PeriodicWorkRequestBuilder<ReconcileDaysWorker>(12, TimeUnit.HOURS)
    .addTag(RECONCILE_PERIODIC_TAG)
    .build()

object DailyCycleWorkScheduler {
    private const val STARTUP_WORK = "daily-cycle-startup"
    private const val PERIODIC_WORK = "daily-cycle-periodic"

    fun schedule(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.enqueueUniqueWork(
            STARTUP_WORK,
            ExistingWorkPolicy.KEEP,
            startupReconcileRequest(),
        )
        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicReconcileRequest(),
        )
    }
}
