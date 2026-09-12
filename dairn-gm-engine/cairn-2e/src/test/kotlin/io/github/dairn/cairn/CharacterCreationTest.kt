package io.github.dairn.cairn

import io.github.dairn.core.*
import kotlin.test.*

class CharacterCreationTest {
    @Test
    fun `all official backgrounds provide names and starting equipment`() {
        assertEquals(20, CairnCharacterData.backgrounds.size)
        CairnCharacterData.backgrounds.forEach { background ->
            assertEquals(10, background.names.size, background.name)
            assertTrue(background.startingEquipment.isNotEmpty(), background.name)
            assertFalse("3d6 Gold Pieces" in background.startingEquipment, background.name)
        }
        assertEquals(20, CairnCharacterData.lifepaths.size)
        CairnCharacterData.lifepaths.values.forEach { lifepath ->
            assertEquals(2, lifepath.tables.size, lifepath.id)
            lifepath.tables.forEach { table ->
                assertEquals("d6", table.die)
                assertEquals((1..6).toList(), table.results.map { it.roll })
            }
        }
    }

    @Test
    fun `creation chooses background name and optional attribute swap`() {
        val started = CairnCharacterCreation.transition(
            CharacterCreationState.NotStarted,
            CharacterCreationCommand.Start(1, listOf(8, 12, 15), 4, 27, 11),
        )
        assertFalse(started.completed)
        assertEquals("Hestia", started.pendingChoice?.options?.first())
        val named = CairnCharacterCreation.transition(started.state, CharacterCreationCommand.ChooseName(1))
        val completed = CairnCharacterCreation.transition(named.state, CharacterCreationCommand.SwapAttributes(0 to 2))
        val character = (completed.state as CharacterCreationState.Completed).character
        assertTrue(completed.completed)
        assertEquals("Basil", character.name)
        assertEquals("Aurifex", character.background)
        assertEquals(27, character.age)
        assertEquals(listOf(15, 12, 8), Attribute.entries.map(character.attributes::getValue))
        assertEquals(4, character.hitProtection)
        assertEquals(11, character.goldPieces)
        assertTrue("Lantern" in character.inventory)
    }

    @Test
    fun `creation can keep attributes in rolled order`() {
        val started = CairnCharacterCreation.transition(
            CharacterCreationState.NotStarted,
            CharacterCreationCommand.Start(20, listOf(8, 12, 15), 4, 27, 11),
        )
        val named = CairnCharacterCreation.transition(started.state, CharacterCreationCommand.ChooseName(0))
        val completed = CairnCharacterCreation.transition(named.state, CharacterCreationCommand.SwapAttributes(null))
        val character = (completed.state as CharacterCreationState.Completed).character
        assertEquals(listOf(8, 12, 15), Attribute.entries.map(character.attributes::getValue))
        assertEquals("Scrivener", character.background)
    }

    @Test
    fun `swap rejects the same attribute twice`() {
        val state = CharacterCreationState.AwaitingSwap(CairnCharacterData.backgrounds.first(), "Hestia", listOf(8, 12, 15), 4, 27, 11)
        assertFailsWith<IllegalArgumentException> {
            CairnCharacterCreation.transition(state, CharacterCreationCommand.SwapAttributes(0 to 0))
        }
    }
}
