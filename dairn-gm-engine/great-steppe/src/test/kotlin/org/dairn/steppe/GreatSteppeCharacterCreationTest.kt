package org.dairn.steppe

import org.dairn.core.InteractiveStep
import org.dairn.core.InteractiveProcess
import org.dairn.core.ProcessRequest
import org.dairn.core.ProcessResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class GreatSteppeCharacterCreationTest {
    @Test
    fun `foundling creation resolves canonical tables and a personal omen`() {
        val process = GreatSteppeCharacterCreation("ru")
        var step: InteractiveStep = process.start()
        step = choose(process, step, "18")
        step = enter(process, step, "Айбек")
        step = roll(process, step, mapOf(
            "water" to 1, "food" to 2, "fire" to 3,
            "weapon" to 4, "travel-gear" to 5, "tool" to 6,
        ))
        step = choose(process, step, "water-fire")
        step = enter(process, step, "Жизнь в караване")
        step = roll(process, step, mapOf("str" to 8, "dex" to 12, "wil" to 10, "hp" to 4))
        step = choose(process, step, "str-wil")
        step = roll(process, step, mapOf(
            "trait-physique" to 1, "trait-skin" to 2, "trait-hair" to 3,
            "trait-face" to 4, "trait-speech" to 5, "trait-clothing" to 6,
            "trait-virtue" to 7, "trait-flaw" to 8, "bond" to 9,
        ))
        step = roll(process, step, mapOf("age" to 25))
        step = roll(process, step, mapOf("omen" to 1))

        val completed = assertIs<InteractiveStep.Completed>(step)
        val character = assertIs<GreatSteppeCharacter>(completed.artifact)
        assertEquals(listOf(10, 12, 8), character.attributes)
        assertEquals("Айбек", character.name)
        assertEquals(18, character.lifePath.roll)
        assertEquals(25, character.age)
        assertEquals(1, assertNotNull(character.secretOmen).roll)
        assertFalse(character.fields.any { it.label.contains("общее знамение", ignoreCase = true) })
    }

    @Test
    fun `non-foundling creation completes without a personal omen`() {
        val process = GreatSteppeCharacterCreation("ru")
        var step: InteractiveStep = process.start()
        step = choose(process, step, "1")
        step = enter(process, step, "Баян")
        step = roll(process, step, mapOf(
            "water" to 1, "food" to 1, "fire" to 1,
            "weapon" to 1, "travel-gear" to 1, "tool" to 1,
        ))
        step = choose(process, step, "keep")
        step = enter(process, step, "")
        step = roll(process, step, mapOf("str" to 9, "dex" to 10, "wil" to 11, "hp" to 3))
        step = choose(process, step, "keep")
        step = roll(process, step, mapOf(
            "trait-physique" to 1, "trait-skin" to 1, "trait-hair" to 1,
            "trait-face" to 1, "trait-speech" to 1, "trait-clothing" to 1,
            "trait-virtue" to 1, "trait-flaw" to 1, "bond" to 1,
        ))
        step = roll(process, step, mapOf("age" to 30))

        val character = assertIs<GreatSteppeCharacter>(assertIs<InteractiveStep.Completed>(step).artifact)
        assertEquals(null, character.secretOmen)
    }

    private fun choose(process: InteractiveProcess, step: InteractiveStep, option: String): InteractiveStep {
        val waiting = assertIs<InteractiveStep.Waiting>(step)
        val request = assertIs<ProcessRequest.Choose>(waiting.request)
        return process.advance(waiting.state, ProcessResponse.Selected(request.id, listOf(option)))
    }

    private fun enter(process: InteractiveProcess, step: InteractiveStep, value: String): InteractiveStep {
        val waiting = assertIs<InteractiveStep.Waiting>(step)
        val request = assertIs<ProcessRequest.EnterText>(waiting.request)
        return process.advance(waiting.state, ProcessResponse.TextEntered(request.id, value))
    }

    private fun roll(process: InteractiveProcess, step: InteractiveStep, totals: Map<String, Int>): InteractiveStep {
        val waiting = assertIs<InteractiveStep.Waiting>(step)
        val request = assertIs<ProcessRequest.Roll>(waiting.request)
        return process.advance(waiting.state, ProcessResponse.Rolled(request.id, totals))
    }
}
