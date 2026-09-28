package org.dairn.steppe

import org.dairn.core.Dice
import org.dairn.rules.AbilityCheckResult
import org.dairn.rules.AbilityScore
import org.dairn.rules.DairnRulesModule

/** Reads the score from this snapshot; callers supply an updated snapshot after score loss. */
fun GreatSteppeCharacter.abilityScore(ability: AbilityScore): Int = attributes[
    when (ability) {
        AbilityScore.STR -> 0
        AbilityScore.DEX -> 1
        AbilityScore.WIL -> 2
    }
]

/** Resolves an already designated check; the caller owns eligibility and consequences. */
fun GreatSteppeCharacter.check(ability: AbilityScore, dice: Dice): AbilityCheckResult =
    DairnRulesModule.check(ability, abilityScore(ability), dice)

fun GreatSteppeCharacter.resolveCheck(ability: AbilityScore, roll: Int): AbilityCheckResult =
    DairnRulesModule.resolveCheck(ability, abilityScore(ability), roll)
