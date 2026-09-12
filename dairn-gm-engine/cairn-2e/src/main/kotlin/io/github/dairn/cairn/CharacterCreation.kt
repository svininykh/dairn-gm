package io.github.dairn.cairn

import io.github.dairn.core.*

object CairnCharacterCreation : CharacterCreationProcess {
    const val NAME_CHOICE = "cairn-2e.character.name"
    const val ATTRIBUTE_SWAP_CHOICE = "cairn-2e.character.attribute-swap"

    override fun transition(state: CharacterCreationState, command: CharacterCreationCommand): TransitionResult<CharacterCreationState> = when {
        state is CharacterCreationState.NotStarted && command is CharacterCreationCommand.Start -> start(command)
        state is CharacterCreationState.AwaitingName && command is CharacterCreationCommand.ChooseName -> chooseName(state, command)
        state is CharacterCreationState.AwaitingSwap && command is CharacterCreationCommand.SwapAttributes -> swap(state, command)
        else -> throw IllegalArgumentException("Command ${command::class.simpleName} is invalid for ${state::class.simpleName}")
    }

    private fun start(command: CharacterCreationCommand.Start): TransitionResult<CharacterCreationState> {
        require(command.backgroundRoll in 1..CairnBackgrounds.all.size) { "Background roll must be between 1 and 20" }
        require(command.scores.size == Attribute.entries.size && command.scores.all { it in 3..18 })
        require(command.hitProtection in 1..6)
        require(command.age in 12..50)
        require(command.goldPieces in 3..18)
        val state = CharacterCreationState.AwaitingName(
            CairnBackgrounds.all[command.backgroundRoll - 1], command.scores, command.hitProtection, command.age, command.goldPieces,
        )
        return TransitionResult(state, pendingChoice = Choice.Required(NAME_CHOICE, state.background.names))
    }

    private fun chooseName(state: CharacterCreationState.AwaitingName, command: CharacterCreationCommand.ChooseName): TransitionResult<CharacterCreationState> {
        require(command.index in state.background.names.indices)
        val next = CharacterCreationState.AwaitingSwap(
            state.background, state.background.names[command.index], state.scores, state.hitProtection, state.age, state.goldPieces,
        )
        return TransitionResult(
            next,
            pendingChoice = Choice.Required(ATTRIBUTE_SWAP_CHOICE, listOf("keep", "str-dex", "str-wil", "dex-wil")),
        )
    }

    private fun swap(state: CharacterCreationState.AwaitingSwap, command: CharacterCreationCommand.SwapAttributes): TransitionResult<CharacterCreationState> {
        val scores = state.scores.toMutableList()
        command.positions?.let { (first, second) ->
            require(first in scores.indices && second in scores.indices && first != second)
            val value = scores[first]
            scores[first] = scores[second]
            scores[second] = value
        }
        val character = Character(
            name = state.name,
            age = state.age,
            background = state.background.name,
            attributes = Attribute.entries.zip(scores).toMap(),
            hitProtection = state.hitProtection,
            goldPieces = state.goldPieces,
            inventory = state.background.startingEquipment,
        )
        return TransitionResult(CharacterCreationState.Completed(character), completed = true)
    }
}
