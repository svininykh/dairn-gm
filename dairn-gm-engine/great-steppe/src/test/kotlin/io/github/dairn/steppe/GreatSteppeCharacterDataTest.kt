package io.github.dairn.steppe

import kotlin.test.Test
import kotlin.test.assertEquals

class GreatSteppeCharacterDataTest {
    @Test
    fun `canonical character tables are complete`() {
        assertEquals(20, GreatSteppeCharacterData.lifePaths.size)
        assertEquals(6, GreatSteppeCharacterData.inventory.size)
        assertEquals(8, GreatSteppeCharacterData.traits.size)
        assertEquals(20, GreatSteppeCharacterData.bonds.size)
        assertEquals(20, GreatSteppeCharacterData.omens.size)
        assertEquals("3fabe55fa366c3706a5c54614b1682bd2f7cb5d6", GreatSteppeCharacterData.SOURCE_REVISION)
    }
}
