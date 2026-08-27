package com.mushind.mind.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class LogicalDayPolicy(
    val rolloverTime: LocalTime = LocalTime.MIDNIGHT,
)

class LogicalDayResolver(
    private val policy: LogicalDayPolicy = LogicalDayPolicy(),
) {
    fun resolve(instant: Instant, zoneId: ZoneId): LocalDate {
        val local = instant.atZone(zoneId)
        return if (local.toLocalTime() < policy.rolloverTime) {
            local.toLocalDate().minusDays(1)
        } else {
            local.toLocalDate()
        }
    }

    fun startsAt(day: LocalDate, zoneId: ZoneId): Instant =
        day.atTime(policy.rolloverTime).atZone(zoneId).toInstant()

    fun endsAt(day: LocalDate, zoneId: ZoneId): Instant = startsAt(day.plusDays(1), zoneId)
}

