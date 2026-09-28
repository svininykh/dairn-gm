package org.dairn.steppe

import org.dairn.core.Dice
import org.dairn.core.DiceRoll
import org.dairn.rules.AbilityScore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GreatSteppeAbilityChecksTest {
    private val character = GreatSteppeCharacterGenerator()
        .generate(dice = Dice { count, sides -> DiceRoll(List(count) { 1 }, sides) })
        .copy(attributes = listOf(8, 12, 16))

    @Test
    fun `checks use the selected ability from the character snapshot`() {
        for ((ability, score) in listOf(AbilityScore.STR to 8, AbilityScore.DEX to 12, AbilityScore.WIL to 16)) {
            val result = character.resolveCheck(ability, score)
            assertEquals(ability, result.ability)
            assertEquals(score, result.score)
            assertTrue(result.successful)
            assertFalse(character.resolveCheck(ability, score + 1).successful)
        }
    }

    @Test
    fun `check uses changed scores and injected dice without changing the character`() {
        val injured = character.copy(attributes = listOf(8, 5, 16))
        val dice = Dice { count, sides ->
            assertEquals(1, count)
            assertEquals(20, sides)
            DiceRoll(listOf(6), sides)
        }
        assertTrue(character.check(AbilityScore.DEX, dice).successful)
        assertFalse(injured.check(AbilityScore.DEX, dice).successful)
        assertEquals(listOf(8, 5, 16), injured.attributes)
    }
}
