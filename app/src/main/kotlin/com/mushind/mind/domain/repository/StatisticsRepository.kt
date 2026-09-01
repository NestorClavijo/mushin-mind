package com.mushind.mind.domain.repository

import com.mushind.mind.domain.model.StatisticsDashboard
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

interface StatisticsRepository {
    fun observeDashboard(referenceAt: Instant, zoneId: ZoneId): Flow<StatisticsDashboard>
}
