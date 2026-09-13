package io.github.dairn.steppe

import io.github.dairn.core.GameEffect
import io.github.dairn.core.InventoryEntry
import io.github.dairn.core.InventoryLoad
import io.github.dairn.core.Rule

data class GainFatigueContext(
    val inventory: InventoryLoad,
    val amount: Int = 1,
) {
    init {
        require(amount > 0) { "Fatigue amount must be positive" }
    }
}

sealed interface InventoryEffect : GameEffect {
    /** The shell must decide which equipment to leave before applying the fatigue. */
    data class RequireFreeSlots(val amount: Int) : InventoryEffect
    data object SetHitProtectionToZero : InventoryEffect
}

object GreatSteppeGainFatigueRule : Rule<GainFatigueContext> {
    override fun evaluate(context: GainFatigueContext): List<GameEffect> = buildList {
        requireGreatSteppeCapacity(context.inventory)
        val missingSlots = (context.amount - context.inventory.freeSlots).coerceAtLeast(0)
        if (missingSlots > 0) add(InventoryEffect.RequireFreeSlots(missingSlots))
        add(SurvivalEffect.GainFatigue(context.amount))
    }
}

object GreatSteppeFullInventoryRule : Rule<InventoryLoad> {
    override fun evaluate(context: InventoryLoad): List<GameEffect> {
        requireGreatSteppeCapacity(context)
        return if (context.isFull) listOf(InventoryEffect.SetHitProtectionToZero) else emptyList()
    }
}

private fun requireGreatSteppeCapacity(inventory: InventoryLoad) {
    require(inventory.capacity == GREAT_STEPPE_INVENTORY_CAPACITY) {
        "Great Steppe inventory capacity must be $GREAT_STEPPE_INVENTORY_CAPACITY"
    }
}

const val GREAT_STEPPE_INVENTORY_CAPACITY = 10

private const val FATIGUE_ENTRY_PREFIX = "great-steppe.fatigue."

/** Great Steppe represents each Fatigue as a namespaced one-slot core inventory entry. */
fun greatSteppeFatigueEntry(sequence: Int): InventoryEntry {
    require(sequence > 0) { "Fatigue sequence must be positive" }
    return InventoryEntry("$FATIGUE_ENTRY_PREFIX$sequence", slots = 1)
}

fun InventoryEntry.isGreatSteppeFatigue(): Boolean = id.startsWith(FATIGUE_ENTRY_PREFIX)
