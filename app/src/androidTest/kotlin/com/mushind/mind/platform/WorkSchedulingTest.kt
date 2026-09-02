package com.mushind.mind.platform

import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.data.preferences.ReminderSettings
import com.mushind.mind.platform.notifications.PlanningReminderWorker
import com.mushind.mind.platform.notifications.planningReminderRequest
import com.mushind.mind.platform.workers.RECONCILE_PERIODIC_TAG
import com.mushind.mind.platform.workers.RECONCILE_STARTUP_TAG
import com.mushind.mind.platform.workers.ReconcileDaysWorker
import com.mushind.mind.platform.workers.periodicReconcileRequest
import com.mushind.mind.platform.workers.startupReconcileRequest
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkSchedulingTest {
    @Test
    fun dailyCycleRequestsUseExpectedWorkerTagsAndCadence() {
        val startup = startupReconcileRequest()
        val periodic = periodicReconcileRequest()

        assertEquals(ReconcileDaysWorker::class.java.name, startup.workSpec.workerClassName)
        assertTrue(startup.tags.contains(RECONCILE_STARTUP_TAG))
        assertEquals(ReconcileDaysWorker::class.java.name, periodic.workSpec.workerClassName)
        assertTrue(periodic.tags.contains(RECONCILE_PERIODIC_TAG))
        assertEquals(TimeUnit.HOURS.toMillis(12), periodic.workSpec.intervalDuration)
    }

    @Test
    fun planningReminderTargetsNextConfiguredLocalTime() {
        val clock = object : ClockProvider {
            override fun now(): Instant = Instant.parse("2026-09-01T15:00:00Z")
            override fun zoneId(): ZoneId = ZoneId.of("America/Bogota")
        }

        val request = planningReminderRequest(ReminderSettings(true, 11, 30), clock)

        assertEquals(PlanningReminderWorker::class.java.name, request.workSpec.workerClassName)
        assertEquals(TimeUnit.MINUTES.toMillis(90), request.workSpec.initialDelay)
        assertEquals(TimeUnit.HOURS.toMillis(24), request.workSpec.intervalDuration)
    }
}
