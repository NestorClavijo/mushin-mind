package com.mushind.mind.domain.usecase

import com.mushind.mind.domain.model.ChallengeQuestionType
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeGeneratorTest {
    @Test
    fun `challenge alternates arithmetic and sequence questions`() {
        val startedAt = Instant.parse("2026-08-26T15:00:00Z")
        val challenge = ChallengeGenerator().generate(
            "attempt-1",
            "com.example.social",
            startedAt,
            LocalDate.parse("2026-08-27"),
            ChallengePolicy(4, Duration.ofMinutes(3)),
        )

        assertEquals(4, challenge.questions.size)
        assertEquals(ChallengeQuestionType.ARITHMETIC, challenge.questions[0].type)
        assertEquals(ChallengeQuestionType.SEQUENCE, challenge.questions[1].type)
        assertEquals(startedAt.plusSeconds(180), challenge.minimumCompletesAt)
        assertTrue(challenge.questions.all { it.correctAnswer in it.options })
    }
}
