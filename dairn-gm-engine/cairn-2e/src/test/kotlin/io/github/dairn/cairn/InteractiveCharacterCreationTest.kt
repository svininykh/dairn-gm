package io.github.dairn.cairn

import io.github.dairn.core.Attribute
import io.github.dairn.core.InteractiveStep
import io.github.dairn.core.ProcessRequest
import io.github.dairn.core.ProcessResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InteractiveCharacterCreationTest {
    @Test
    fun `process requests rolls and choices without shell-owned rules`() {
        val started = CairnInteractiveCharacterCreation.start()
        val rolls = assertIs<ProcessRequest.Roll>(started.request)
        assertEquals(
            listOf("background", "str", "dex", "wil", "hp", "age", "gold", "lifepath-past", "lifepath-present"),
            rolls.rolls.map { it.id },
        )

        val awaitingName = assertIs<InteractiveStep.Waiting>(
            CairnInteractiveCharacterCreation.advance(
                started.state,
                ProcessResponse.Rolled(
                    rolls.id,
                    mapOf(
                        "background" to 1,
                        "str" to 8,
                        "dex" to 12,
                        "wil" to 15,
                        "hp" to 4,
                        "age" to 27,
                        "gold" to 11,
                        "lifepath-past" to 1,
                        "lifepath-present" to 6,
                    ),
                ),
            ),
        )
        val names = assertIs<ProcessRequest.Choose>(awaitingName.request)
        assertEquals("Hestia", names.options.first().label)

        val awaitingSwap = assertIs<InteractiveStep.Waiting>(
            CairnInteractiveCharacterCreation.advance(
                awaitingName.state,
                ProcessResponse.Selected(names.id, listOf("1")),
            ),
        )
        val swap = assertIs<ProcessRequest.Choose>(awaitingSwap.request)
        val completed = assertIs<InteractiveStep.Completed>(
            CairnInteractiveCharacterCreation.advance(
                awaitingSwap.state,
                ProcessResponse.Selected(swap.id, listOf("str-wil")),
            ),
        )
        val character = assertIs<CairnCharacter>(completed.artifact)
        assertEquals("Basil", character.name)
        assertEquals(listOf(15, 12, 8), Attribute.entries.map(character.attributes::getValue))
        assertEquals(listOf(1, 6), character.lifepath.map { it.roll })
    }
}
