package com.mushind.mind.domain.usecase

import com.mushind.mind.core.common.IdProvider
import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.DailyPlan
import com.mushind.mind.domain.model.DailyPlanStatus
import com.mushind.mind.domain.model.LogicalDayPolicy
import com.mushind.mind.domain.model.LogicalDayResolver
import com.mushind.mind.domain.repository.DailyCycleRepository
import com.mushind.mind.domain.repository.DailyPlanRepository
import com.mushind.mind.domain.repository.DailyReconciliation
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyCycleTest {
    private val zone = ZoneId.of("America/Bogota")
    private val instant = Instant.parse("2026-08-26T15:00:00Z")
    private val clock = FixedClock(instant, zone)
    private val resolver = LogicalDayResolver()

    @Test
    fun `preparing tomorrow is idempotent`() = runBlocking {
        val repository = FakeDailyPlanRepository()
        val useCase = PrepareTomorrowPlan(repository, clock, resolver, IdProvider { "plan-1" })

        val first = useCase()
        val second = useCase()

        assertSame(first, second)
        assertEquals(LocalDate.parse("2026-08-27"), first.date)
        assertEquals(DailyPlanStatus.DRAFT, first.status)
        assertEquals(1, repository.plans.size)
    }

    @Test
    fun `confirmed plan activates on its logical day`() = runBlocking {
        val repository = FakeDailyCycleRepository(
            lastDay = LocalDate.parse("2026-08-25"),
            planDays = mutableMapOf(LocalDate.parse("2026-08-26") to DailyPlanStatus.CONFIRMED),
        )

        val result = ReconcileDays(repository, clock, resolver)()

        assertTrue(result.single().activatedPlan)
        assertEquals(DailyPlanStatus.ACTIVE, repository.planDays[LocalDate.parse("2026-08-26")])
    }

    @Test
    fun `closing the same day twice does not duplicate summary`() = runBlocking {
        val yesterday = LocalDate.parse("2026-08-25")
        val repository = FakeDailyCycleRepository(
            lastDay = yesterday,
            planDays = mutableMapOf(yesterday to DailyPlanStatus.ACTIVE),
        )
        val useCase = ReconcileDays(repository, clock, resolver)

        useCase()
        val secondRun = useCase()

        assertEquals(1, repository.summaryDays.size)
        assertEquals(0, secondRun.single().closedPlans)
    }

    @Test
    fun `daily session from previous day expires`() = runBlocking {
        val repository = FakeDailyCycleRepository(
            lastDay = LocalDate.parse("2026-08-25"),
            activeDailySessions = mutableSetOf(LocalDate.parse("2026-08-25")),
        )

        val result = ReconcileDays(repository, clock, resolver)()

        assertEquals(1, result.single().expiredDailySessions)
        assertTrue(repository.activeDailySessions.isEmpty())
    }

    @Test
    fun `reconciling after three missed days processes every boundary once`() = runBlocking {
        val repository = FakeDailyCycleRepository(lastDay = LocalDate.parse("2026-08-23"))

        val result = ReconcileDays(repository, clock, resolver)()

        assertEquals(
            listOf("2026-08-24", "2026-08-25", "2026-08-26"),
            result.map { it.day.toString() },
        )
        assertEquals(LocalDate.parse("2026-08-26"), repository.lastDay)
    }

    @Test
    fun `custom rollover keeps early morning in previous logical day`() {
        val customResolver = LogicalDayResolver(LogicalDayPolicy(LocalTime.of(4, 0)))
        val earlyMorning = Instant.parse("2026-08-26T07:30:00Z")

        assertEquals(
            LocalDate.parse("2026-08-25"),
            customResolver.resolve(earlyMorning, zone),
        )
    }
}

private class FixedClock(
    private val instant: Instant,
    private val zone: ZoneId,
) : ClockProvider {
    override fun now(): Instant = instant
    override fun zoneId(): ZoneId = zone
}

private class FakeDailyPlanRepository : DailyPlanRepository {
    val plans = linkedMapOf<LocalDate, DailyPlan>()
    private val flows = mutableMapOf<LocalDate, MutableStateFlow<DailyPlan?>>()

    override fun observeByDate(date: LocalDate): Flow<DailyPlan?> =
        flows.getOrPut(date) { MutableStateFlow(plans[date]) }

    override suspend fun getByDate(date: LocalDate): DailyPlan? = plans[date]

    override suspend fun create(plan: DailyPlan) {
        plans[plan.date] = plan
        flows.getOrPut(plan.date) { MutableStateFlow(null) }.value = plan
    }

    override suspend fun confirm(planId: String, confirmedAt: Instant): Boolean {
        val entry = plans.entries.firstOrNull { it.value.id == planId } ?: return false
        if (entry.value.status != DailyPlanStatus.DRAFT) return false
        val confirmed = entry.value.copy(
            status = DailyPlanStatus.CONFIRMED,
            confirmedAt = confirmedAt,
        )
        plans[entry.key] = confirmed
        flows.getOrPut(entry.key) { MutableStateFlow(null) }.value = confirmed
        return true
    }
}

private class FakeDailyCycleRepository(
    var lastDay: LocalDate?,
    val planDays: MutableMap<LocalDate, DailyPlanStatus> = mutableMapOf(),
    val activeDailySessions: MutableSet<LocalDate> = mutableSetOf(),
) : DailyCycleRepository {
    val summaryDays = mutableSetOf<LocalDate>()

    override suspend fun lastReconciledDay(): LocalDate? = lastDay

    override suspend fun reconcileDay(day: LocalDate, transitionAt: Instant): DailyReconciliation {
        var closed = 0
        planDays.filter { (planDay, status) ->
            planDay < day && status in setOf(DailyPlanStatus.ACTIVE, DailyPlanStatus.CONFIRMED)
        }.keys.toList().forEach { planDay ->
            if (summaryDays.add(planDay)) closed++
            planDays[planDay] = DailyPlanStatus.CLOSED
        }

        val sessionsBefore = activeDailySessions.count { it < day }
        activeDailySessions.removeAll { it < day }
        val activated = planDays[day] == DailyPlanStatus.CONFIRMED
        if (activated) planDays[day] = DailyPlanStatus.ACTIVE
        lastDay = day

        return DailyReconciliation(day, closed, sessionsBefore, activated)
    }
}
