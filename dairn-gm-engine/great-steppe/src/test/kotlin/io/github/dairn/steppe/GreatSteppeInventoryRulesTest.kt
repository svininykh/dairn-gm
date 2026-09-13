package io.github.dairn.steppe

import io.github.dairn.core.InventoryEntry
import io.github.dairn.core.InventoryLoad
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GreatSteppeInventoryRulesTest {
    @Test
    fun `fatigue fits into a free inventory slot`() {
        val load = InventoryLoad(10, listOf(InventoryEntry("gear", 8)), fatigue = 1)
        assertEquals(
            listOf(SurvivalEffect.GainFatigue(1)),
            GreatSteppeGainFatigueRule.evaluate(GainFatigueContext(load)),
        )
    }

    @Test
    fun `fatigue requests enough space when inventory is full`() {
        val load = InventoryLoad(10, listOf(InventoryEntry("gear", 9)), fatigue = 1)
        assertEquals(
            listOf(InventoryEffect.RequireFreeSlots(1), SurvivalEffect.GainFatigue(1)),
            GreatSteppeGainFatigueRule.evaluate(GainFatigueContext(load)),
        )
    }

    @Test
    fun `multiple fatigue requests only the missing slots`() {
        val load = InventoryLoad(10, listOf(InventoryEntry("gear", 7)), fatigue = 1)
        assertEquals(
            listOf(InventoryEffect.RequireFreeSlots(1), SurvivalEffect.GainFatigue(3)),
            GreatSteppeGainFatigueRule.evaluate(GainFatigueContext(load, amount = 3)),
        )
    }

    @Test
    fun `only a full resulting inventory reduces hit protection to zero`() {
        val full = InventoryLoad(10, listOf(InventoryEntry("gear", 8)), fatigue = 2)
        val notFull = InventoryLoad(10, listOf(InventoryEntry("gear", 7)), fatigue = 2)
        assertEquals(
            listOf(InventoryEffect.SetHitProtectionToZero),
            GreatSteppeFullInventoryRule.evaluate(full),
        )
        assertTrue(GreatSteppeFullInventoryRule.evaluate(notFull).isEmpty())
    }

    @Test
    fun `Great Steppe rules enforce ten inventory slots`() {
        val differentRulesetLoad = InventoryLoad(12, emptyList())
        assertFailsWith<IllegalArgumentException> {
            GreatSteppeGainFatigueRule.evaluate(GainFatigueContext(differentRulesetLoad))
        }
    }
}
