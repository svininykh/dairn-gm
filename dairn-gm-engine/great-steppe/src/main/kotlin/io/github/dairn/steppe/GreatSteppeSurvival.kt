package io.github.dairn.steppe

import io.github.dairn.core.GameEffect
import io.github.dairn.core.Rule

enum class DeprivationReason {
    WATER,
    FOOD,
    FULL_REST,
}

data class SurvivalDayContext(
    val hasWater: Boolean,
    val hasFood: Boolean,
    val receivedFullRest: Boolean,
    val wasDeprived: Boolean,
    /** Consecutive deprived days completed before the day being evaluated. */
    val previousDeprivedDays: Int,
) {
    init {
        require(previousDeprivedDays >= 0)
        require(wasDeprived == (previousDeprivedDays > 0)) {
            "Previous deprived days must agree with the prior condition"
        }
    }
}

sealed interface SurvivalEffect : GameEffect {
    data class BecomeDeprived(val reasons: Set<DeprivationReason>) : SurvivalEffect
    data object CeaseBeingDeprived : SurvivalEffect
    data object BlockRecovery : SurvivalEffect
    data class GainFatigue(val amount: Int) : SurvivalEffect {
        init {
            require(amount > 0)
        }
    }
}

/**
 * Evaluates one day of the confirmed Great Steppe deprivation procedure.
 *
 * Fire is deliberately not a direct deprivation reason in the canonical text. A caller may decide
 * from the fiction whether the character received full rest, including whether a fire was needed.
 */
object GreatSteppeSurvivalDayRule : Rule<SurvivalDayContext> {
    override fun evaluate(context: SurvivalDayContext): List<GameEffect> {
        val reasons = buildSet {
            if (!context.hasWater) add(DeprivationReason.WATER)
            if (!context.hasFood) add(DeprivationReason.FOOD)
            if (!context.receivedFullRest) add(DeprivationReason.FULL_REST)
        }
        if (reasons.isEmpty()) {
            return if (context.wasDeprived) listOf(SurvivalEffect.CeaseBeingDeprived) else emptyList()
        }
        return buildList {
            add(SurvivalEffect.BecomeDeprived(reasons))
            add(SurvivalEffect.BlockRecovery)
            if (context.previousDeprivedDays >= 1) add(SurvivalEffect.GainFatigue(1))
        }
    }
}
