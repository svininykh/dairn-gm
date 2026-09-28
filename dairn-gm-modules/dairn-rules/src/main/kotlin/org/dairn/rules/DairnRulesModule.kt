package org.dairn.rules

import org.dairn.core.DairnModule
import org.dairn.core.Dice
import org.dairn.core.ModuleId
import org.dairn.core.ModuleInfo

enum class AbilityScore { STR, DEX, WIL }

/** A resolved check, not permission to act or an automatic world-state change. */
data class AbilityCheckResult(
    val ability: AbilityScore,
    val score: Int,
    val roll: Int,
) {
    init {
        require(roll in 1..20) { "An Ability Score check requires a d20 result" }
    }

    val successful: Boolean get() = roll == 1 || (roll != 20 && roll <= score)
}

/** Chapter 3 checks. The caller decides whether a check is appropriate and supplies the current score. */
object DairnRulesModule : DairnModule {
    override val info = ModuleInfo(ModuleId("dairn-rules"), "0.1.0", "DAIRN: Common Rules")

    /** Pure resolution, also suitable for caller-supplied rolls and replay. No character range is imposed. */
    fun resolveCheck(ability: AbilityScore, score: Int, roll: Int): AbilityCheckResult =
        AbilityCheckResult(ability, score, roll)

    fun check(ability: AbilityScore, score: Int, dice: Dice): AbilityCheckResult {
        val roll = dice.roll(1, 20)
        require(roll.sides == 20 && roll.dice.size == 1) { "Expected exactly one d20" }
        return resolveCheck(ability, score, roll.dice.single())
    }
}
