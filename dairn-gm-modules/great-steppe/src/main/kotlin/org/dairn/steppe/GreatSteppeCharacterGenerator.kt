package org.dairn.steppe

import org.dairn.core.Dice
import org.dairn.core.InteractiveStep
import org.dairn.core.ProcessRequest
import org.dairn.core.ProcessResponse
import org.dairn.core.RandomDice

/** Values authored by a reader and therefore not invented by automatic generation. */
data class GreatSteppeGenerationInput(
    val name: String? = null,
    val experienceDetail: String? = null,
)

/**
 * Runs the existing Great Steppe creation process without a reader.
 * Rule-defined values are rolled; optional swaps use their neutral `keep` choice.
 */
class GreatSteppeCharacterGenerator(
    private val languageTag: String = "ru",
) {
    fun generate(
        input: GreatSteppeGenerationInput = GreatSteppeGenerationInput(),
        dice: Dice = RandomDice(),
    ): GreatSteppeCharacter {
        val process = GreatSteppeCharacterCreation(languageTag)
        var step: InteractiveStep = process.start()
        while (step is InteractiveStep.Waiting) {
            val waiting = step
            step = process.advance(waiting.state, automaticResponse(waiting.request, input, dice))
        }
        return (step as InteractiveStep.Completed).artifact as GreatSteppeCharacter
    }

    private fun automaticResponse(
        request: ProcessRequest,
        input: GreatSteppeGenerationInput,
        dice: Dice,
    ): ProcessResponse = when (request) {
        is ProcessRequest.Roll -> ProcessResponse.Rolled(
            request.id,
            request.rolls.associate { spec ->
                spec.id to (dice.roll(spec.dice.count, spec.dice.sides).total + spec.dice.modifier)
            },
        )
        is ProcessRequest.Choose -> ProcessResponse.Selected(request.id, listOf(defaultChoice(request)))
        is ProcessRequest.EnterText -> ProcessResponse.TextEntered(
            request.id,
            when (request.id.value) {
                "great-steppe.character.name" -> input.name.orEmpty()
                "great-steppe.character.experience-detail" -> input.experienceDetail.orEmpty()
                else -> error("Unsupported automatic text request: ${request.id.value}")
            },
        )
    }

    private fun defaultChoice(request: ProcessRequest.Choose): String = when {
        request.id.value.endsWith(".supplies-swap") || request.id.value.endsWith(".attribute-swap") -> "keep"
        request.options.any { it.id == "roll" } -> "roll"
        else -> error("Unsupported automatic choice request: ${request.id.value}")
    }
}
