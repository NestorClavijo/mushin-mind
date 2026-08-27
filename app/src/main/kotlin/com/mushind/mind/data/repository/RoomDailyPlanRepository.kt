package com.mushind.mind.data.repository

import com.mushind.mind.data.local.dao.DailyPlanDao
import com.mushind.mind.data.local.entity.DailyPlanEntity
import com.mushind.mind.domain.model.DailyPlan
import com.mushind.mind.domain.repository.DailyPlanRepository
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDailyPlanRepository @Inject constructor(
    private val dao: DailyPlanDao,
) : DailyPlanRepository {
    override fun observeByDate(date: LocalDate): Flow<DailyPlan?> =
        dao.observeByDate(date).map { it?.toDomain() }

    override suspend fun getByDate(date: LocalDate): DailyPlan? = dao.getByDate(date)?.toDomain()

    override suspend fun create(plan: DailyPlan) = dao.insert(plan.toEntity())

    override suspend fun confirm(planId: String, confirmedAt: Instant): Boolean =
        dao.confirm(planId, confirmedAt) == 1
}

private fun DailyPlanEntity.toDomain() = DailyPlan(
    id = id,
    date = date,
    status = status,
    createdAt = createdAt,
    confirmedAt = confirmedAt,
    closedAt = closedAt,
)

private fun DailyPlan.toEntity() = DailyPlanEntity(
    id = id,
    date = date,
    status = status,
    createdAt = createdAt,
    confirmedAt = confirmedAt,
    closedAt = closedAt,
)

