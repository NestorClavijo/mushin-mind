package com.mushind.mind.core.time

import java.time.Instant
import java.time.ZoneId

interface ClockProvider {
    fun now(): Instant
    fun zoneId(): ZoneId
}

class SystemClockProvider : ClockProvider {
    override fun now(): Instant = Instant.now()
    override fun zoneId(): ZoneId = ZoneId.systemDefault()
}

