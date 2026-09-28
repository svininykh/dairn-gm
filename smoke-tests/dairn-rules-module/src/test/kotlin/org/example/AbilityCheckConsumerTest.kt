package org.example

import org.dairn.core.Dice
import org.dairn.core.DiceRoll
import org.dairn.rules.AbilityScore
import org.dairn.rules.DairnRulesModule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class AbilityCheckConsumerTest {
    @Test
    fun `external application checks DEX and WIL without setting modules`() {
        val dice = Dice { _, _ -> DiceRoll(listOf(12), 20) }
        assertTrue(DairnRulesModule.check(AbilityScore.DEX, 12, dice).successful)
        assertFalse(DairnRulesModule.check(AbilityScore.WIL, 11, dice).successful)
        for (name in listOf("org.dairn.steppe.GreatSteppeModule", "org.dairn.cairn.Cairn2eModule")) {
            assertFailsWith<ClassNotFoundException> { Class.forName(name) }
        }
    }
}
