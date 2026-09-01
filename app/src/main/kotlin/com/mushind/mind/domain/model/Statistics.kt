package com.mushind.mind.domain.model

import java.time.LocalDate

data class PeriodStatistics(
    val plannedTasks: Int = 0,
    val completedTasks: Int = 0,
    val pointsEarned: Int = 0,
    val pointsSpent: Int = 0,
    val purchasedMinutes: Int = 0,
    val emergencies: Int = 0,
) {
    val netPoints: Int get() = pointsEarned - pointsSpent
    val completionPercent: Int
        get() = if (plannedTasks == 0) 0 else completedTasks * 100 / plannedTasks
}

data class DailyStatistics(
    val day: LocalDate,
    val totals: PeriodStatistics,
)

data class AppStatistics(
    val packageName: String,
    val displayName: String,
    val unlocks: Int,
    val purchasedMinutes: Int,
    val pointsSpent: Int,
    val emergencies: Int,
)

data class StatisticsDashboard(
    val today: DailyStatistics,
    val week: PeriodStatistics,
    val days: List<DailyStatistics>,
    val apps: List<AppStatistics>,
    val transactions: List<PointTransaction>,
    val tasks: List<Task>,
    val emergencyUnlocks: List<EmergencyUnlock>,
)
