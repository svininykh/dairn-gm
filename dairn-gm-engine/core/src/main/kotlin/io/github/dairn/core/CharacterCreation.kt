package io.github.dairn.core

sealed interface CharacterCreationState : ProcessState {
    data object NotStarted : CharacterCreationState
    data class AwaitingName(
        val background: CharacterBackground,
        val scores: List<Int>,
        val hitProtection: Int,
        val age: Int,
        val goldPieces: Int,
    ) : CharacterCreationState
    data class AwaitingSwap(
        val background: CharacterBackground,
        val name: String,
        val scores: List<Int>,
        val hitProtection: Int,
        val age: Int,
        val goldPieces: Int,
    ) : CharacterCreationState
    data class Completed(val character: Character) : CharacterCreationState
}

sealed interface CharacterCreationCommand : ProcessCommand {
    data class Start(
        val backgroundRoll: Int,
        val scores: List<Int>,
        val hitProtection: Int,
        val age: Int,
        val goldPieces: Int,
    ) : CharacterCreationCommand
    data class ChooseName(val index: Int) : CharacterCreationCommand
    /** Null keeps scores in order; otherwise the two zero-based positions are swapped. */
    data class SwapAttributes(val positions: Pair<Int, Int>?) : CharacterCreationCommand
}

interface CharacterCreationProcess : GameProcess<CharacterCreationState, CharacterCreationCommand>

/** Capability implemented only by rulesets that support character creation. */
interface CharacterCreationModule : DairnModule {
    val characterCreation: CharacterCreationProcess
}
