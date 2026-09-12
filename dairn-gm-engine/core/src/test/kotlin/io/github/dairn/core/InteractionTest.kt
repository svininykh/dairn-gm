package io.github.dairn.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class InteractionTest {
    @Test
    fun `dice expression validates its shape`() {
        assertEquals(DiceExpression(3, 6), DiceExpression(count = 3, sides = 6))
        assertFailsWith<IllegalArgumentException> { DiceExpression(0, 6) }
        assertFailsWith<IllegalArgumentException> { DiceExpression(1, 1) }
    }

    @Test
    fun `request only accepts a response with the same id`() {
        val request = ProcessRequest.EnterText(RequestId("character.name"), "Name")
        request.requireMatching(ProcessResponse.TextEntered(RequestId("character.name"), "Aru"))
        assertFailsWith<IllegalArgumentException> {
            request.requireMatching(ProcessResponse.TextEntered(RequestId("character.age"), "20"))
        }
    }

    @Test
    fun `choice option identifiers must be unique`() {
        assertFailsWith<IllegalArgumentException> {
            ProcessRequest.Choose(
                RequestId("character.origin"),
                "Choose origin",
                listOf(ChoiceOption("steppe", "Steppe"), ChoiceOption("steppe", "Other Steppe")),
            )
        }
    }
}

