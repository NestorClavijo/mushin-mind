package com.mushind.mind.domain.usecase

import com.mushind.mind.core.time.ClockProvider
import com.mushind.mind.domain.model.AppRule
import com.mushind.mind.domain.model.AppRuleType
import com.mushind.mind.domain.model.InstalledApplication
import com.mushind.mind.domain.model.RestrictedApp
import com.mushind.mind.domain.model.RuleStrictness
import com.mushind.mind.domain.repository.AppCatalogRepository
import com.mushind.mind.domain.repository.AppRulesRepository
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class AppRulesTest {
    private val now = Instant.parse("2026-08-26T15:00:00Z")
    private val compare = CompareRuleStrictness()

    @Test(expected = IllegalArgumentException::class)
    fun `zero point rule is rejected`() {
        rule(cost = 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duration below allowed range is rejected`() {
        rule(duration = 4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `duration above allowed range is rejected`() {
        rule(duration = 241)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `daily rule rejects a duration`() {
        rule(type = AppRuleType.UNTIL_END_OF_DAY, duration = 15)
    }

    @Test
    fun `own application is excluded and search ignores case`() = runBlocking {
        val repository = FakeCatalogRepository(
            ownPackageName = "com.mushind.mind",
            apps = listOf(
                app("com.mushind.mind", "Mind"),
                app("com.example.music", "Música"),
                app("com.example.video", "Vídeo"),
            ),
        )

        val result = GetInstalledApps(repository)("MÚS")

        assertEquals(listOf("com.example.music"), result.map { it.packageName })
    }

    @Test
    fun `higher cost and shorter duration is stricter`() {
        val result = compare(rule(cost = 20, duration = 30), rule(cost = 25, duration = 20))

        assertEquals(RuleStrictness.STRICTER, result)
    }

    @Test
    fun `lower cost or longer duration is permissive`() {
        assertEquals(
            RuleStrictness.MORE_PERMISSIVE,
            compare(rule(cost = 20, duration = 30), rule(cost = 15, duration = 30)),
        )
        assertEquals(
            RuleStrictness.MORE_PERMISSIVE,
            compare(rule(cost = 20, duration = 30), rule(cost = 20, duration = 45)),
        )
    }

    @Test
    fun `daily to temporary with same cost is stricter`() {
        val result = compare(
            rule(type = AppRuleType.UNTIL_END_OF_DAY, duration = null),
            rule(type = AppRuleType.TEMPORARY_SESSION, duration = 30),
        )

        assertEquals(RuleStrictness.STRICTER, result)
    }

    @Test
    fun `new restriction is persisted`() = runBlocking {
        val repository = FakeRulesRepository()
        val result = CreateAppRule(repository, FixedAppClock(now))(
            app("com.example.music", "Música"),
            AppRuleType.TEMPORARY_SESSION,
            20,
            15,
        )

        assertSame(RuleChangeResult.Saved, result)
        assertEquals(20, repository.saved?.rule?.costPoints)
    }

    @Test
    fun `permissive update is not persisted`() = runBlocking {
        val originalRule = rule(cost = 20, duration = 15)
        val existing = RestrictedApp(
            originalRule.packageName, "Música", true, false, originalRule, now, now,
        )
        val repository = FakeRulesRepository(existing)

        val result = UpdateAppRule(repository, compare, FixedAppClock(now))(
            existing,
            rule(cost = 10, duration = 15),
        )

        assertSame(RuleChangeResult.RequiresChallenge, result)
        assertNull(repository.saved)
    }

    @Test
    fun `cost increase is persisted immediately`() = runBlocking {
        val originalRule = rule(cost = 30, duration = 20)
        val existing = RestrictedApp(
            originalRule.packageName, "Música", true, false, originalRule, now, now,
        )
        val repository = FakeRulesRepository(existing)

        val result = UpdateAppRule(repository, compare, FixedAppClock(now))(
            existing,
            rule(cost = 50, duration = 20),
        )

        assertSame(RuleChangeResult.Saved, result)
        assertEquals(50, repository.saved?.rule?.costPoints)
    }

    private fun rule(
        type: AppRuleType = AppRuleType.TEMPORARY_SESSION,
        cost: Int = 20,
        duration: Int? = 15,
    ) = AppRule("com.example.music", type, cost, duration, now, now)

    private fun app(packageName: String, name: String) =
        InstalledApplication(packageName, name, isSystemApp = false, isCritical = false)
}

private class FakeCatalogRepository(
    override val ownPackageName: String,
    private val apps: List<InstalledApplication>,
) : AppCatalogRepository {
    override suspend fun getInstalledApps() = apps
}

private class FakeRulesRepository(
    private val existing: RestrictedApp? = null,
) : AppRulesRepository {
    var saved: RestrictedApp? = null
    override fun observeRestrictedApps(): Flow<List<RestrictedApp>> = flowOf(listOfNotNull(existing))
    override suspend fun getRestrictedApp(packageName: String) = existing
    override suspend fun saveRestrictedApp(app: RestrictedApp) { saved = app }
}

private class FixedAppClock(private val instant: Instant) : ClockProvider {
    override fun now() = instant
    override fun zoneId(): ZoneId = ZoneId.of("America/Bogota")
}
