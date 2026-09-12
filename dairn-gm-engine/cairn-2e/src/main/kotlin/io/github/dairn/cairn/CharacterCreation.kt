package io.github.dairn.cairn

import io.github.dairn.core.Attribute
import io.github.dairn.core.Character
import io.github.dairn.core.CharacterCreationCommand
import io.github.dairn.core.CharacterCreationProcess
import io.github.dairn.core.CharacterCreationState
import io.github.dairn.core.Choice
import io.github.dairn.core.TransitionResult

object CairnCharacterCreation : CharacterCreationProcess {
    const val ATTRIBUTE_ASSIGNMENT_CHOICE = "cairn-2e.character.attributes"

    override fun transition(
        state: CharacterCreationState,
        command: CharacterCreationCommand,
    ): TransitionResult<CharacterCreationState> = when {
        state is CharacterCreationState.NotStarted && command is CharacterCreationCommand.Start -> start(command)
        state is CharacterCreationState.AwaitingAssignment && command is CharacterCreationCommand.AssignAttributes -> assign(state, command)
        else -> throw IllegalArgumentException("Command ${command::class.simpleName} is invalid for ${state::class.simpleName}")
    }

    private fun start(command: CharacterCreationCommand.Start): TransitionResult<CharacterCreationState> {
        require(command.scores.size == Attribute.entries.size) { "Exactly three attribute scores are required" }
        require(command.scores.all { it in 3..18 }) { "Attribute scores must be between 3 and 18" }
        require(command.hitProtection in 1..6) { "Hit Protection must be between 1 and 6" }
        val state = CharacterCreationState.AwaitingAssignment(command.scores, command.hitProtection)
        return TransitionResult(
            state = state,
            pendingChoice = Choice.Required(
                id = ATTRIBUTE_ASSIGNMENT_CHOICE,
                options = command.scores,
                minimum = Attribute.entries.size,
                maximum = Attribute.entries.size,
            ),
        )
    }

    private fun assign(
        state: CharacterCreationState.AwaitingAssignment,
        command: CharacterCreationCommand.AssignAttributes,
    ): TransitionResult<CharacterCreationState> {
        require(command.order.sorted() == state.scores.indices.toList()) {
            "Assignment must contain each score index exactly once"
        }
        val scores = Attribute.entries.zip(command.order.map(state.scores::get)).toMap()
        return TransitionResult(
            state = CharacterCreationState.Completed(Character(scores, state.hitProtection)),
            completed = true,
        )
    }
}
