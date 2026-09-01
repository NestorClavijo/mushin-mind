package com.mushind.mind.data.repository

import com.mushind.mind.data.local.dao.AppLabelRow
import com.mushind.mind.data.local.db.MindDatabase
import com.mushind.mind.data.local.entity.EmergencyUnlockEntity
import com.mushind.mind.data.local.entity.PointTransactionEntity
import com.mushind.mind.data.local.entity.TaskEntity
import com.mushind.mind.data.local.entity.UnlockSessionEntity
import com.mushind.mind.data.local.entity.UnlockSessionStatus
import com.mushind.mind.data.local.entity.UnlockSessionType
import com.mushind.mind.domain.model.EmergencyUnlock
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.model.PointTransaction
import com.mushind.mind.domain.model.StatisticsDashboard
import com.mushind.mind.domain.model.Task
import com.mushind.mind.domain.model.UnlockSession
import com.mushind.mind.domain.model.UnlockSessionKind
import com.mushind.mind.domain.model.UnlockSessionState
import com.mushind.mind.domain.repository.StatisticsRepository
import com.mushind.mind.domain.usecase.CalculateStatistics
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class RoomStatisticsRepository @Inject constructor(
    database: MindDatabase,
    private val logicalDayResolver: LogicalDayResolver,
) : StatisticsRepository {
    private val dao = database.statisticsDao()
    private val calculator = CalculateStatistics(logicalDayResolver)

    override fun observeDashboard(referenceAt: Instant, zoneId: ZoneId): Flow<StatisticsDashboard> {
        val today = logicalDayResolver.resolve(referenceAt, zoneId)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekEnd = weekStart.plusDays(7)
        val fromInstant = logicalDayResolver.startsAt(weekStart, zoneId)
        val toInstant = logicalDayResolver.startsAt(weekEnd, zoneId)
        val weekly = combine(
            dao.observeTasks(weekStart, weekEnd),
            dao.observeTransactions(fromInstant, toInstant),
            dao.observeSessions(weekStart, weekEnd),
            dao.observeEmergencies(fromInstant, toInstant),
            dao.observeAppLabels(),
        ) { tasks, transactions, sessions, emergencies, labels ->
            WeeklyData(tasks, transactions, sessions, emergencies, labels)
        }
        return combine(
            weekly,
            dao.observeRecentTransactions(HISTORY_LIMIT),
            dao.observeRecentTasks(HISTORY_LIMIT),
            dao.observeRecentEmergencies(HISTORY_LIMIT),
        ) { data, recentTransactions, recentTasks, recentEmergencies ->
            calculator(
                today = today,
                weekStart = weekStart,
                zoneId = zoneId,
                tasks = data.tasks.map(TaskEntity::toDomain),
                transactions = data.transactions.map(PointTransactionEntity::toDomain),
                sessions = data.sessions.map(UnlockSessionEntity::toDomain),
                emergencyUnlocks = data.emergencies.map(EmergencyUnlockEntity::toDomain),
                appNames = data.labels.associate(AppLabelRow::toPair),
                recentTransactions = recentTransactions.map(PointTransactionEntity::toDomain),
                recentTasks = recentTasks.map(TaskEntity::toDomain),
                recentEmergencies = recentEmergencies.map(EmergencyUnlockEntity::toDomain),
            )
        }
    }

    private companion object {
        const val HISTORY_LIMIT = 30
    }
}

private data class WeeklyData(
    val tasks: List<TaskEntity>,
    val transactions: List<PointTransactionEntity>,
    val sessions: List<UnlockSessionEntity>,
    val emergencies: List<EmergencyUnlockEntity>,
    val labels: List<AppLabelRow>,
)

private fun AppLabelRow.toPair() = packageName to displayName

private fun TaskEntity.toDomain() = Task(
    id, planId, title, description, rewardPoints, plannedDate, status, origin,
    generatesPoints, createdAt, completedAt,
)

private fun PointTransactionEntity.toDomain() = PointTransaction(
    id, type, amount, referenceId, description, createdAt,
)

private fun EmergencyUnlockEntity.toDomain() = EmergencyUnlock(
    id, sessionId, packageName, reason, durationMinutes, configuredPenaltyPoints,
    appliedPenaltyPoints, balanceBefore, balanceAfter, createdAt,
)

private fun UnlockSessionEntity.toDomain() = UnlockSession(
    id = id,
    packageName = packageName,
    type = when (type) {
        UnlockSessionType.TEMPORARY -> UnlockSessionKind.TEMPORARY
        UnlockSessionType.UNTIL_END_OF_DAY -> UnlockSessionKind.UNTIL_END_OF_DAY
        UnlockSessionType.EMERGENCY -> UnlockSessionKind.EMERGENCY
    },
    appliedRuleType = if (type == UnlockSessionType.EMERGENCY) null else ruleType,
    startsAt = startsAt,
    endsAt = endsAt,
    logicalDay = logicalDay,
    costPoints = costPoints,
    status = when (status) {
        UnlockSessionStatus.ACTIVE -> UnlockSessionState.ACTIVE
        UnlockSessionStatus.EXPIRED -> UnlockSessionState.EXPIRED
        UnlockSessionStatus.ENDED -> UnlockSessionState.ENDED
        UnlockSessionStatus.CANCELLED -> UnlockSessionState.CANCELLED
    },
)
