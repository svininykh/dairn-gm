package io.github.dairn.cairn

import io.github.dairn.core.Attribute
import io.github.dairn.core.CharacterCreationCommand
import io.github.dairn.core.CharacterCreationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CharacterCreationTest {
    @Test
    fun `creation waits for attribute assignment then completes`() {
        val started = CairnCharacterCreation.transition(
            CharacterCreationState.NotStarted,
            CharacterCreationCommand.Start(listOf(8, 12, 15), hitProtection = 4),
        )
        assertFalse(started.completed)
        assertEquals(listOf(8, 12, 15), started.pendingChoice?.options)

        val completed = CairnCharacterCreation.transition(
            started.state,
            CharacterCreationCommand.AssignAttributes(listOf(2, 0, 1)),
        )
        assertTrue(completed.completed)
        val character = (completed.state as CharacterCreationState.Completed).character
        assertEquals(15, character.attributes[Attribute.STRENGTH])
        assertEquals(8, character.attributes[Attribute.DEXTERITY])
        assertEquals(12, character.attributes[Attribute.WILLPOWER])
        assertEquals(4, character.hitProtection)
    }

    @Test
    fun `assignment rejects duplicate score indexes`() {
        val state = CharacterCreationState.AwaitingAssignment(listOf(8, 12, 15), 4)
        assertFailsWith<IllegalArgumentException> {
            CairnCharacterCreation.transition(state, CharacterCreationCommand.AssignAttributes(listOf(0, 0, 1)))
        }
    }
}
