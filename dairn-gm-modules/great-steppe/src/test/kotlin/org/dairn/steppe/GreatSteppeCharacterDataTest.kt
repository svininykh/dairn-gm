package org.dairn.steppe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GreatSteppeCharacterDataTest {
    @Test
    fun `canonical character tables are complete`() {
        assertEquals(20, GreatSteppeCharacterData.lifePaths.size)
        assertEquals(6, GreatSteppeCharacterData.inventory.size)
        assertEquals(8, GreatSteppeCharacterData.traits.size)
        assertEquals(20, GreatSteppeCharacterData.bonds.size)
        assertEquals(20, GreatSteppeCharacterData.omens.size)
        assertEquals("e8efc7f1351a7fbfeb1876579e7b8ab1c5633256", GreatSteppeCharacterData.SOURCE_REVISION)
    }

    @Test
    fun `every possible starting inventory fits the character capacity`() {
        val rows = GreatSteppeCharacterData.inventory
        val maximumLoad = listOf(
            rows.maxOf { it.waterSlots },
            rows.maxOf { it.foodSlots },
            rows.maxOf { it.fireSlots },
            rows.maxOf { it.weaponSlots },
            rows.maxOf { it.travelGearSlots },
            rows.maxOf { it.toolSlots },
        ).sum()

        assertEquals(GREAT_STEPPE_INVENTORY_CAPACITY, maximumLoad)
    }

    @Test
    fun `public omen resolver resolves a localized table result`() {
        val omen = GreatSteppeModule.resolveOmen(1, "ru")

        assertEquals(1, omen.roll)
        assertEquals("Река течёт вспять", omen.name)
        assertFailsWith<IllegalArgumentException> { GreatSteppeModule.resolveOmen(0) }
    }
}
