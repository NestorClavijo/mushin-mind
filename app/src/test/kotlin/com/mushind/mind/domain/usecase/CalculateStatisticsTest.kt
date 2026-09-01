package com.mushind.mind.domain.usecase

import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.EmergencyUnlock
import com.mushind.mind.domain.model.PointTransaction
import com.mushind.mind.domain.model.PointTransactionType
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.TaskStatus
import com.mushind.mind.domain.model.UnlockSession
import com.mushind.mind.domain.model.UnlockSessionKind
import com.mushind.mind.domain.model.UnlockSessionState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateStatisticsTest {
    private val calculator = CalculateStatistics()
    private val zone = ZoneId.of("UTC")
    private val weekStart = LocalDate.parse("2026-08-24")
    private val today = LocalDate.parse("2026-08-26")

    @Test
    fun `known data aggregates day and week from ledger`() {
        val dashboard = calculator(
            today = today,
            weekStart = weekStart,
            zoneId = zone,
            tasks = listOf(
                task("task-1", today, TaskStatus.COMPLETED),
                task("task-2", today, TaskStatus.PENDING),
                task("task-3", weekStart, TaskStatus.COMPLETED),
            ),
            transactions = listOf(
                transaction("reward-1", PointTransactionType.TASK_REWARD, 50, "task-1", "2026-08-26T10:00:00Z"),
                transaction("unlock-1", PointTransactionType.APP_UNLOCK, -20, "session-1", "2026-08-26T11:00:00Z"),
                transaction("penalty-1", PointTransactionType.EMERGENCY_PENALTY, -10, "event-1", "2026-08-26T12:00:00Z"),
                transaction("reward-2", PointTransactionType.TASK_REWARD, 10, "task-3", "2026-08-24T10:00:00Z"),
            ),
            sessions = listOf(session("session-1", 20)),
            emergencyUnlocks = listOf(emergency("event-1")),
            appNames = mapOf(PACKAGE_NAME to "Video"),
        )

        assertEquals(2, dashboard.today.totals.plannedTasks)
        assertEquals(1, dashboard.today.totals.completedTasks)
        assertEquals(50, dashboard.today.totals.pointsEarned)
        assertEquals(30, dashboard.today.totals.pointsSpent)
        assertEquals(20, dashboard.today.totals.netPoints)
        assertEquals(20, dashboard.today.totals.purchasedMinutes)
        assertEquals(1, dashboard.today.totals.emergencies)
        assertEquals(3, dashboard.week.plannedTasks)
        assertEquals(60, dashboard.week.pointsEarned)
        assertEquals(30, dashboard.week.pointsSpent)
    }

    @Test
    fun `empty week returns seven zero days`() {
        val dashboard = calculator(today, weekStart, zone, emptyList(), emptyList(), emptyList(), emptyList())

        assertEquals(7, dashboard.days.size)
        assertEquals(0, dashboard.week.netPoints)
        assertEquals(0, dashboard.week.completionPercent)
        assertTrue(dashboard.apps.isEmpty())
    }

    @Test
    fun `multiple unlocks are counted per ledger transaction`() {
        val dashboard = calculator(
            today,
            weekStart,
            zone,
            tasks = emptyList(),
            transactions = listOf(
                transaction("unlock-1", PointTransactionType.APP_UNLOCK, -20, "session-1", "2026-08-26T11:00:00Z"),
                transaction("unlock-2", PointTransactionType.APP_UNLOCK, -15, "session-2", "2026-08-26T12:00:00Z"),
            ),
            sessions = listOf(session("session-1", 20), session("session-2", 15)),
            emergencyUnlocks = emptyList(),
            appNames = mapOf(PACKAGE_NAME to "Video"),
        )

        assertEquals(2, dashboard.apps.single().unlocks)
        assertEquals(35, dashboard.apps.single().purchasedMinutes)
        assertEquals(35, dashboard.apps.single().pointsSpent)
    }

    @Test
    fun `transactions at midnight belong to different logical days`() {
        val dashboard = calculator(
            today,
            weekStart,
            zone,
            tasks = emptyList(),
            transactions = listOf(
                transaction("before", PointTransactionType.CORRECTION, 10, "manual-1", "2026-08-26T23:59:59Z"),
                transaction("after", PointTransactionType.CORRECTION, 20, "manual-2", "2026-08-27T00:00:00Z"),
            ),
            sessions = emptyList(),
            emergencyUnlocks = emptyList(),
        )

        assertEquals(10, dashboard.days.single { it.day == today }.totals.pointsEarned)
        assertEquals(20, dashboard.days.single { it.day == today.plusDays(1) }.totals.pointsEarned)
    }

    private fun task(id: String, day: LocalDate, status: TaskStatus) = Task(
        id = id,
        planId = "plan-$day",
        title = id,
        rewardPoints = 10,
        plannedDate = day,
        status = status,
        createdAt = Instant.parse("${day}T08:00:00Z"),
        completedAt = if (status == TaskStatus.COMPLETED) Instant.parse("${day}T10:00:00Z") else null,
    )

    private fun transaction(
        id: String,
        type: PointTransactionType,
        amount: Int,
        referenceId: String,
        at: String,
    ) = PointTransaction(id, type, amount, referenceId, id, Instant.parse(at))

    private fun session(id: String, minutes: Long) = UnlockSession(
        id = id,
        packageName = PACKAGE_NAME,
        type = UnlockSessionKind.TEMPORARY,
        appliedRuleType = AppRuleType.TEMPORARY_SESSION,
        startsAt = Instant.parse("2026-08-26T11:00:00Z"),
        endsAt = Instant.parse("2026-08-26T11:00:00Z").plusSeconds(minutes * 60),
        logicalDay = today,
        costPoints = minutes.toInt(),
        status = UnlockSessionState.EXPIRED,
    )

    private fun emergency(id: String) = EmergencyUnlock(
        id = id,
        sessionId = "emergency-session",
        packageName = PACKAGE_NAME,
        reason = "Necesario",
        durationMinutes = 10,
        configuredPenaltyPoints = 10,
        appliedPenaltyPoints = 10,
        balanceBefore = 30,
        balanceAfter = 20,
        createdAt = Instant.parse("2026-08-26T12:00:00Z"),
    )

    private companion object {
        const val PACKAGE_NAME = "com.example.video"
    }
}
