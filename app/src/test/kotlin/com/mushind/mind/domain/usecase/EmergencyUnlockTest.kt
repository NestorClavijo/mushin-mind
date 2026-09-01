package com.mushind.mind.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmergencyUnlockTest {
    @Test
    fun `hold confirms only after required duration`() {
        val gate = HoldConfirmationGate(requiredMillis = 3_000)

        gate.start(atMillis = 1_000)
        assertFalse(gate.release(atMillis = 3_999))

        gate.start(atMillis = 5_000)
        assertTrue(gate.release(atMillis = 8_000))
    }

    @Test
    fun `cancelling hold performs no confirmation`() {
        val gate = HoldConfirmationGate(requiredMillis = 3_000)

        gate.start(atMillis = 1_000)
        gate.cancel()

        assertFalse(gate.release(atMillis = 10_000))
    }
}
