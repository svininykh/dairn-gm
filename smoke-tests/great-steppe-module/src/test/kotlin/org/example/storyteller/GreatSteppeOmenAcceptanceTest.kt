package org.example.storyteller

import org.dairn.steppe.GreatSteppeModule
import org.dairn.steppe.GreatSteppeCharacterGenerator
import org.dairn.steppe.check
import org.dairn.core.Dice
import org.dairn.core.DiceRoll
import org.dairn.rules.AbilityScore
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GreatSteppeOmenAcceptanceTest {
    @Test
    fun `published Great Steppe exposes shared ability checks transitively`() {
        val dice = Dice { count, sides -> DiceRoll(List(count) { 1 }, sides) }
        val character = GreatSteppeCharacterGenerator().generate(dice = dice)
        assertTrue(character.check(AbilityScore.DEX, dice).successful)
        assertTrue(character.check(AbilityScore.WIL, dice).successful)
    }

    @Test
    fun `an external StoryTeller resolves every published omen`() {
        (1..20).forEach { roll ->
            val omen = GreatSteppeModule.resolveOmen(roll, "ru")

            assertTrue(omen.name.isNotBlank())
            assertTrue(omen.description.isNotBlank())
        }
        assertFailsWith<IllegalArgumentException> { GreatSteppeModule.resolveOmen(0, "ru") }
        assertFailsWith<IllegalArgumentException> { GreatSteppeModule.resolveOmen(21, "ru") }
    }
}
