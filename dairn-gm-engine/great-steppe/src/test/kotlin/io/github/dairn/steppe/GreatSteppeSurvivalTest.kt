package io.github.dairn.steppe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GreatSteppeSurvivalTest {
    @Test
    fun `first day without water causes deprivation and blocks recovery`() {
        val effects = GreatSteppeSurvivalDayRule.evaluate(
            SurvivalDayContext(
                hasWater = false,
                hasFood = true,
                receivedFullRest = true,
                wasDeprived = false,
                previousDeprivedDays = 0,
            ),
        )
        assertEquals(
            listOf(
                SurvivalEffect.BecomeDeprived(setOf(DeprivationReason.WATER)),
                SurvivalEffect.BlockRecovery,
            ),
            effects,
        )
    }

    @Test
    fun `each deprived day after the first adds one fatigue`() {
        val effects = GreatSteppeSurvivalDayRule.evaluate(
            SurvivalDayContext(false, false, false, wasDeprived = true, previousDeprivedDays = 2),
        )
        assertEquals(SurvivalEffect.GainFatigue(1), effects.last())
        assertEquals(
            setOf(DeprivationReason.WATER, DeprivationReason.FOOD, DeprivationReason.FULL_REST),
            (effects.first() as SurvivalEffect.BecomeDeprived).reasons,
        )
    }

    @Test
    fun `meeting all needs ends prior deprivation`() {
        val effects = GreatSteppeSurvivalDayRule.evaluate(
            SurvivalDayContext(true, true, true, wasDeprived = true, previousDeprivedDays = 3),
        )
        assertEquals(listOf(SurvivalEffect.CeaseBeingDeprived), effects)
    }

    @Test
    fun `fire is not invented as an independent deprivation reason`() {
        assertTrue(DeprivationReason.entries.none { it.name == "FIRE" })
    }
}
