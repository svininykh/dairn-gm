package org.dairn.rules

import org.dairn.core.Dice
import org.dairn.core.DiceRoll
import org.dairn.core.RandomDice
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class AbilityCheckTest {
    @Test
    fun `all abilities succeed below and at the score and fail above it`() {
        for (ability in AbilityScore.entries) {
            assertTrue(DairnRulesModule.resolveCheck(ability, 10, 9).successful)
            assertTrue(DairnRulesModule.resolveCheck(ability, 10, 10).successful)
            assertFalse(DairnRulesModule.resolveCheck(ability, 10, 11).successful)
        }
    }

    @Test
    fun `natural one and twenty override the comparison`() {
        for (ability in AbilityScore.entries) {
            assertTrue(DairnRulesModule.resolveCheck(ability, 0, 1).successful)
            assertFalse(DairnRulesModule.resolveCheck(ability, 0, 2).successful)
            assertTrue(DairnRulesModule.resolveCheck(ability, 20, 19).successful)
            assertFalse(DairnRulesModule.resolveCheck(ability, 20, 20).successful)
            assertFalse(DairnRulesModule.resolveCheck(ability, 25, 20).successful)
        }
    }

    @Test
    fun `check consumes exactly one d20 and retains supplied context`() {
        var calls = 0
        val dice = Dice { count, sides ->
            calls++
            assertEquals(1, count)
            assertEquals(20, sides)
            DiceRoll(listOf(12), 20)
        }
        val result = DairnRulesModule.check(AbilityScore.DEX, 12, dice)
        assertEquals(AbilityCheckResult(AbilityScore.DEX, 12, 12), result)
        assertTrue(result.successful)
        assertEquals(1, calls)
    }

    @Test
    fun `invalid supplied rolls and malformed dice responses are rejected`() {
        for (roll in listOf(0, 21)) {
            assertFailsWith<IllegalArgumentException> {
                DairnRulesModule.resolveCheck(AbilityScore.STR, 10, roll)
            }
        }
        for (roll in listOf(DiceRoll(listOf(3), 6), DiceRoll(listOf(3, 4), 20))) {
            assertFailsWith<IllegalArgumentException> {
                DairnRulesModule.check(AbilityScore.WIL, 10, Dice { _, _ -> roll })
            }
        }
    }

    @Test
    fun `seeded dice reproduce the same check sequence`() {
        fun sequence() = RandomDice(Random(42)).let { dice ->
            List(30) { DairnRulesModule.check(AbilityScore.WIL, 12, dice) }
        }
        assertEquals(sequence(), sequence())
    }
}
