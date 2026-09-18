package org.dairn.steppe

import org.dairn.core.Dice
import org.dairn.core.DiceRoll
import org.dairn.core.InteractiveStep
import org.dairn.core.ProcessRequest
import org.dairn.core.ProcessResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GreatSteppeCharacterGeneratorTest {
    @Test
    fun `automatic generation is deterministic and leaves reader-authored values unset`() {
        val generator = GreatSteppeCharacterGenerator()

        val first = generator.generate(dice = MinimumDice)
        val second = generator.generate(dice = MinimumDice)

        assertEquals(first, second)
        assertNull(first.name)
        assertNull(first.lifePath.detail)
        assertEquals(listOf(3, 3, 3), first.attributes)
        assertEquals(1, first.hitProtection)
        assertEquals(12, first.age)
    }

    @Test
    fun `automatic craft character may leave its experience detail unset`() {
        val character = GreatSteppeCharacterGenerator().generate(dice = CraftDice)

        assertEquals(8, character.lifePath.roll)
        assertNull(character.lifePath.detail)
    }

    @Test
    fun `automatic and externally supplied minimum rolls produce the same character`() {
        val automatic = GreatSteppeCharacterGenerator().generate(dice = MinimumDice)
        val process = GreatSteppeCharacterCreation()
        var step: InteractiveStep = process.start()

        while (step is InteractiveStep.Waiting) {
            val waiting = step
            step = process.advance(waiting.state, externalMinimumResponse(waiting.request))
        }

        assertEquals(automatic, (step as InteractiveStep.Completed).artifact)
    }

    private fun externalMinimumResponse(request: ProcessRequest): ProcessResponse = when (request) {
        is ProcessRequest.Roll -> ProcessResponse.Rolled(
            request.id,
            request.rolls.associate { it.id to it.dice.count + it.dice.modifier },
        )
        is ProcessRequest.Choose -> ProcessResponse.Selected(
            request.id,
            listOf(
                when {
                    request.id.value.endsWith(".supplies-swap") || request.id.value.endsWith(".attribute-swap") -> "keep"
                    else -> "roll"
                },
            ),
        )
        is ProcessRequest.EnterText -> ProcessResponse.TextEntered(request.id, "")
    }

    private object MinimumDice : Dice {
        override fun roll(count: Int, sides: Int) = DiceRoll(List(count) { 1 }, sides)
    }

    private object CraftDice : Dice {
        override fun roll(count: Int, sides: Int) = DiceRoll(
            List(count) { if (count == 1 && sides == 20) 8 else 1 },
            sides,
        )
    }
}
