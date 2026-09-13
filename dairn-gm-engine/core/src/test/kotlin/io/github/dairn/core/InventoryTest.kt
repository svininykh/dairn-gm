package io.github.dairn.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InventoryTest {
    @Test
    fun `load includes equipment and fatigue slots`() {
        val load = InventoryLoad(
            capacity = 10,
            entries = listOf(
                InventoryEntry("small", 0),
                InventoryEntry("normal", 1),
                InventoryEntry("bulky", 2),
            ),
            fatigue = 2,
        )
        assertEquals(3, load.equipmentSlots)
        assertEquals(5, load.occupiedSlots)
        assertEquals(5, load.freeSlots)
        assertFalse(load.isFull)
    }

    @Test
    fun `exact capacity is a valid full load`() {
        val load = InventoryLoad(10, listOf(InventoryEntry("gear", 8)), fatigue = 2)
        assertTrue(load.isFull)
    }

    @Test
    fun `load cannot exceed capacity`() {
        assertFailsWith<IllegalArgumentException> {
            InventoryLoad(10, listOf(InventoryEntry("gear", 10)), fatigue = 1)
        }
    }
}
