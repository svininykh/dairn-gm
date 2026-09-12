package io.github.dairn.steppe

import io.github.dairn.core.InteractiveStep
import io.github.dairn.core.ProcessRequest
import io.github.dairn.core.ProcessResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs

class GreatSteppeCharacterCreationTest {
    @Test
    fun `creation uses free text and excludes the group omen from character artifact`() {
        var step: InteractiveStep = GreatSteppeCharacterCreation.start()
        listOf(
            "Подкидыш",
            "Жизнь в караване",
            "Посох, припасы",
            "Айбек",
            "Высокий, спокойный",
            "Долг перед родом",
        ).forEach { value ->
            val waiting = assertIs<InteractiveStep.Waiting>(step)
            val request = assertIs<ProcessRequest.EnterText>(waiting.request)
            step = GreatSteppeCharacterCreation.advance(
                waiting.state,
                ProcessResponse.TextEntered(request.id, value),
            )
        }

        val rollsWaiting = assertIs<InteractiveStep.Waiting>(step)
        val rolls = assertIs<ProcessRequest.Roll>(rollsWaiting.request)
        step = GreatSteppeCharacterCreation.advance(
            rollsWaiting.state,
            ProcessResponse.Rolled(
                rolls.id,
                mapOf("str" to 8, "dex" to 12, "wil" to 10, "hp" to 4, "age" to 25),
            ),
        )

        val swapWaiting = assertIs<InteractiveStep.Waiting>(step)
        val swap = assertIs<ProcessRequest.Choose>(swapWaiting.request)
        val completed = assertIs<InteractiveStep.Completed>(
            GreatSteppeCharacterCreation.advance(
                swapWaiting.state,
                ProcessResponse.Selected(swap.id, listOf("str-wil")),
            ),
        )
        val character = assertIs<GreatSteppeCharacter>(completed.artifact)
        assertEquals(listOf(10, 12, 8), character.attributes)
        assertEquals("Айбек", character.name)
        assertFalse(character.fields.any { it.label.contains("Знамение") })
    }
}
