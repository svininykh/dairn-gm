package io.github.dairn.core

sealed interface CharacterCreationState : ProcessState {
    data object NotStarted : CharacterCreationState
    data class AwaitingAssignment(val scores: List<Int>, val hitProtection: Int) : CharacterCreationState
    data class Completed(val character: Character) : CharacterCreationState
}

sealed interface CharacterCreationCommand : ProcessCommand {
    data class Start(val scores: List<Int>, val hitProtection: Int) : CharacterCreationCommand

    /** Indices into the rolled scores, ordered as STR, DEX, WIL. */
    data class AssignAttributes(val order: List<Int>) : CharacterCreationCommand
}

interface CharacterCreationProcess : GameProcess<CharacterCreationState, CharacterCreationCommand>

/** Capability implemented only by rulesets that support character creation. */
interface CharacterCreationModule : DairnModule {
    val characterCreation: CharacterCreationProcess
}

