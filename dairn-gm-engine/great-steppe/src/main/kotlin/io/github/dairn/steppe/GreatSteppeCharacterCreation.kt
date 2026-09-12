package io.github.dairn.steppe

import io.github.dairn.core.*

data class GreatSteppeCharacter(
    val name: String,
    val lifepath: String,
    val experience: String,
    val inventory: String,
    val attributes: List<Int>,
    val hitProtection: Int,
    val appearance: String,
    val bond: String,
    val age: Int,
) : ProcessArtifact {
    override val type = "great-steppe.character"
    override val fields: List<ArtifactField>
        get() = listOf(
            ArtifactField("Имя", name),
            ArtifactField("Жизненный путь", lifepath),
            ArtifactField("Область опыта", experience),
            ArtifactField("Поклажа", inventory),
            ArtifactField("Сила", attributes[0]),
            ArtifactField("Ловкость", attributes[1]),
            ArtifactField("Воля", attributes[2]),
            ArtifactField("Защита (ОЗ)", hitProtection),
            ArtifactField("Образ и черты", appearance),
            ArtifactField("Узы", bond),
            ArtifactField("Возраст", age),
        )
}

private data class Answers(
    val lifepath: String? = null,
    val experience: String? = null,
    val inventory: String? = null,
    val name: String? = null,
    val appearance: String? = null,
    val bond: String? = null,
    val rolls: Map<String, Int>? = null,
)

private data class GreatSteppeState(
    val stage: Int,
    val answers: Answers,
    val request: ProcessRequest,
) : ProcessState

/**
 * The personal part of Great Steppe character creation.
 *
 * Table outcomes are entered as resolved text until the separately authored Great Steppe tables
 * become available as resources. The group Omen belongs to a future group-level process.
 */
object GreatSteppeCharacterCreation : InteractiveProcess {
    override val id = ProcessId("great-steppe.character-creation")

    private val textStages = listOf(
        "lifepath" to "Введите выбранный или выпавший Жизненный путь",
        "experience" to "Опишите область прошлого опыта персонажа",
        "inventory" to "Введите начальную Поклажу, определённую Жизненным путём",
        "name" to "Введите имя персонажа",
        "appearance" to "Введите получившиеся образ и черты персонажа",
        "bond" to "Введите выбранные или выпавшие Узы",
    )

    override fun start(): InteractiveStep.Waiting = waiting(0, Answers())

    override fun advance(state: ProcessState, response: ProcessResponse): InteractiveStep {
        require(state is GreatSteppeState) { "State does not belong to ${id.value}" }
        state.request.requireMatching(response)
        return when {
            state.stage < textStages.size -> acceptText(state, response)
            state.stage == textStages.size -> acceptRolls(state, response)
            else -> acceptSwap(state, response)
        }
    }

    private fun acceptText(state: GreatSteppeState, response: ProcessResponse): InteractiveStep.Waiting {
        require(response is ProcessResponse.TextEntered && response.value.isNotBlank()) { "Non-blank text is required" }
        val next = when (textStages[state.stage].first) {
            "lifepath" -> state.answers.copy(lifepath = response.value)
            "experience" -> state.answers.copy(experience = response.value)
            "inventory" -> state.answers.copy(inventory = response.value)
            "name" -> state.answers.copy(name = response.value)
            "appearance" -> state.answers.copy(appearance = response.value)
            "bond" -> state.answers.copy(bond = response.value)
            else -> error("Unknown stage")
        }
        return waiting(state.stage + 1, next)
    }

    private fun acceptRolls(state: GreatSteppeState, response: ProcessResponse): InteractiveStep.Waiting {
        require(response is ProcessResponse.Rolled) { "Roll response required" }
        val request = state.request as ProcessRequest.Roll
        val expected = request.rolls.map(RollSpec::id).toSet()
        require(response.totals.keys == expected) { "Roll response must contain exactly $expected" }
        request.rolls.forEach { spec ->
            val total = response.totals.getValue(spec.id)
            val range = (spec.dice.count + spec.dice.modifier)..(spec.dice.count * spec.dice.sides + spec.dice.modifier)
            require(total in range) { "${spec.id} result is outside $range" }
        }
        return waiting(state.stage + 1, state.answers.copy(rolls = response.totals))
    }

    private fun acceptSwap(state: GreatSteppeState, response: ProcessResponse): InteractiveStep.Completed {
        require(response is ProcessResponse.Selected && response.optionIds.size == 1) { "One swap choice is required" }
        val choice = response.optionIds.single()
        val request = state.request as ProcessRequest.Choose
        require(request.options.any { it.id == choice }) { "Unknown swap choice: $choice" }
        val rolls = requireNotNull(state.answers.rolls)
        val scores = listOf("str", "dex", "wil").map(rolls::getValue).toMutableList()
        when (choice) {
            "str-dex" -> scores.swap(0, 1)
            "str-wil" -> scores.swap(0, 2)
            "dex-wil" -> scores.swap(1, 2)
        }
        return InteractiveStep.Completed(
            GreatSteppeCharacter(
                name = requireNotNull(state.answers.name),
                lifepath = requireNotNull(state.answers.lifepath),
                experience = requireNotNull(state.answers.experience),
                inventory = requireNotNull(state.answers.inventory),
                attributes = scores,
                hitProtection = rolls.getValue("hp"),
                appearance = requireNotNull(state.answers.appearance),
                bond = requireNotNull(state.answers.bond),
                age = rolls.getValue("age"),
            ),
        )
    }

    private fun waiting(stage: Int, answers: Answers): InteractiveStep.Waiting {
        val request: ProcessRequest = when {
            stage < textStages.size -> ProcessRequest.EnterText(
                RequestId("great-steppe.character.${textStages[stage].first}"),
                textStages[stage].second,
            )
            stage == textStages.size -> ProcessRequest.Roll(
                RequestId("great-steppe.character.rolls"),
                "Бросьте характеристики, ОЗ и возраст",
                listOf(
                    RollSpec("str", DiceExpression(3, 6)),
                    RollSpec("dex", DiceExpression(3, 6)),
                    RollSpec("wil", DiceExpression(3, 6)),
                    RollSpec("hp", DiceExpression(1, 6)),
                    RollSpec("age", DiceExpression(2, 20, 10)),
                ),
            )
            else -> ProcessRequest.Choose(
                RequestId("great-steppe.character.attribute-swap"),
                "Оставьте характеристики по порядку или поменяйте местами любые две",
                listOf(
                    ChoiceOption("keep", "Оставить СИЛ/ЛОВ/ВОЛ"),
                    ChoiceOption("str-dex", "Поменять СИЛ и ЛОВ"),
                    ChoiceOption("str-wil", "Поменять СИЛ и ВОЛ"),
                    ChoiceOption("dex-wil", "Поменять ЛОВ и ВОЛ"),
                ),
            )
        }
        return InteractiveStep.Waiting(GreatSteppeState(stage, answers, request), request)
    }

    private fun <T> MutableList<T>.swap(first: Int, second: Int) {
        val value = this[first]
        this[first] = this[second]
        this[second] = value
    }
}
