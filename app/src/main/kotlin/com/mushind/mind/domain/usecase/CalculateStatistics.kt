package com.mushind.mind.domain.usecase

import com.mushind.mind.domain.model.AppStatistics
import com.mushind.mind.domain.model.DailyStatistics
import com.mushind.mind.domain.model.EmergencyUnlock
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.model.PeriodStatistics
import com.mushind.mind.domain.model.PointTransaction
import com.mushind.mind.domain.model.PointTransactionType
import com.mushind.mind.domain.model.StatisticsDashboard
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.TaskStatus
import com.mushind.mind.domain.model.UnlockSession
import com.mushind.mind.domain.model.UnlockSessionKind
import java.time.LocalDate
import java.time.ZoneId

class CalculateStatistics(
    private val logicalDayResolver: LogicalDayResolver = LogicalDayResolver(),
) {
    operator fun invoke(
        today: LocalDate,
        weekStart: LocalDate,
        zoneId: ZoneId,
        tasks: List<Task>,
        transactions: List<PointTransaction>,
        sessions: List<UnlockSession>,
        emergencyUnlocks: List<EmergencyUnlock>,
        appNames: Map<String, String> = emptyMap(),
        recentTransactions: List<PointTransaction> = transactions,
        recentTasks: List<Task> = tasks,
        recentEmergencies: List<EmergencyUnlock> = emergencyUnlocks,
    ): StatisticsDashboard {
        val weekDays = (0L..6L).map(weekStart::plusDays)
        val daily = weekDays.map { day ->
            DailyStatistics(
                day = day,
                totals = totalsFor(
                    day = day,
                    zoneId = zoneId,
                    tasks = tasks,
                    transactions = transactions,
                    sessions = sessions,
                    emergencyUnlocks = emergencyUnlocks,
                ),
            )
        }
        return StatisticsDashboard(
            today = daily.firstOrNull { it.day == today } ?: DailyStatistics(today, PeriodStatistics()),
            week = daily.map(DailyStatistics::totals).fold(PeriodStatistics(), ::add),
            days = daily,
            apps = appStatistics(sessions, transactions, emergencyUnlocks, appNames),
            transactions = recentTransactions.sortedByDescending(PointTransaction::createdAt),
            tasks = recentTasks.sortedWith(compareByDescending<Task> { it.completedAt ?: it.createdAt }),
            emergencyUnlocks = recentEmergencies.sortedByDescending(EmergencyUnlock::createdAt),
        )
    }

    private fun totalsFor(
        day: LocalDate,
        zoneId: ZoneId,
        tasks: List<Task>,
        transactions: List<PointTransaction>,
        sessions: List<UnlockSession>,
        emergencyUnlocks: List<EmergencyUnlock>,
    ): PeriodStatistics {
        val dayTasks = tasks.filter { it.plannedDate == day }
        val dayTransactions = transactions.filter { logicalDayResolver.resolve(it.createdAt, zoneId) == day }
        return PeriodStatistics(
            plannedTasks = dayTasks.size,
            completedTasks = dayTasks.count { it.status == TaskStatus.COMPLETED },
            pointsEarned = dayTransactions.sumOf { it.amount.coerceAtLeast(0) },
            pointsSpent = dayTransactions.sumOf { (-it.amount).coerceAtLeast(0) },
            purchasedMinutes = sessions
                .filter { it.logicalDay == day && it.type == UnlockSessionKind.TEMPORARY }
                .sumOf { java.time.Duration.between(it.startsAt, it.endsAt).toMinutes().toInt() },
            emergencies = emergencyUnlocks.count { logicalDayResolver.resolve(it.createdAt, zoneId) == day },
        )
    }

    private fun appStatistics(
        sessions: List<UnlockSession>,
        transactions: List<PointTransaction>,
        emergencies: List<EmergencyUnlock>,
        names: Map<String, String>,
    ): List<AppStatistics> {
        val sessionById = sessions.associateBy(UnlockSession::id)
        val packages = sessions.map(UnlockSession::packageName).toSet() +
            emergencies.map(EmergencyUnlock::packageName)
        return packages.map { packageName ->
            val appSessions = sessions.filter { it.packageName == packageName }
            val unlockTransactions = transactions.filter {
                it.type == PointTransactionType.APP_UNLOCK && sessionById[it.referenceId]?.packageName == packageName
            }
            val appEmergencies = emergencies.filter { it.packageName == packageName }
            AppStatistics(
                packageName = packageName,
                displayName = names[packageName] ?: packageName,
                unlocks = unlockTransactions.size,
                purchasedMinutes = appSessions
                    .filter { it.type == UnlockSessionKind.TEMPORARY }
                    .sumOf { java.time.Duration.between(it.startsAt, it.endsAt).toMinutes().toInt() },
                pointsSpent = unlockTransactions.sumOf { (-it.amount).coerceAtLeast(0) } +
                    appEmergencies.sumOf(EmergencyUnlock::appliedPenaltyPoints),
                emergencies = appEmergencies.size,
            )
        }.sortedWith(compareByDescending<AppStatistics> { it.pointsSpent }.thenBy { it.displayName })
    }
}

private fun add(left: PeriodStatistics, right: PeriodStatistics) = PeriodStatistics(
    plannedTasks = left.plannedTasks + right.plannedTasks,
    completedTasks = left.completedTasks + right.completedTasks,
    pointsEarned = left.pointsEarned + right.pointsEarned,
    pointsSpent = left.pointsSpent + right.pointsSpent,
    purchasedMinutes = left.purchasedMinutes + right.purchasedMinutes,
    emergencies = left.emergencies + right.emergencies,
)
