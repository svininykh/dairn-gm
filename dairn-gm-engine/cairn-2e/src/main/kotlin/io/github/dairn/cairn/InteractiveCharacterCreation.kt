package io.github.dairn.cairn

import io.github.dairn.core.*

data class CairnCharacter(
    val name: String,
    val age: Int,
    val background: CharacterBackground,
    val attributes: Map<Attribute, Int>,
    val hitProtection: Int,
    val lifepath: List<LifepathExperience>,
) : ProcessArtifact {
    override val type: String = "cairn-2e.character"
    override val fields: List<ArtifactField>
        get() = listOf(
            ArtifactField("Name", name),
            ArtifactField("Age", age),
            ArtifactField("Background", background.name),
            ArtifactField("STR", attributes.getValue(Attribute.STRENGTH)),
            ArtifactField("DEX", attributes.getValue(Attribute.DEXTERITY)),
            ArtifactField("WIL", attributes.getValue(Attribute.WILLPOWER)),
            ArtifactField("HP", hitProtection),
            ArtifactField("Equipment", background.startingEquipment),
            ArtifactField("Lifepath", lifepath.map(LifepathExperience::text)),
        )
}

private sealed interface CairnCreationState : ProcessState {
    data class AwaitingRolls(val request: ProcessRequest.Roll) : CairnCreationState
    data class AwaitingName(val generated: Generated, val request: ProcessRequest.Choose) : CairnCreationState
    data class AwaitingSwap(
        val generated: Generated,
        val name: String,
        val lifepath: List<LifepathExperience>,
        val request: ProcessRequest.Choose,
    ) : CairnCreationState
}

private data class Generated(
    val background: CharacterBackground,
    val attributes: List<Int>,
    val hitProtection: Int,
    val age: Int,
    val lifepathRolls: List<Int>,
)

object CairnInteractiveCharacterCreation : InteractiveProcess {
    override val id = ProcessId("cairn-2e.character-creation")

    private val rollsRequest = ProcessRequest.Roll(
        id = RequestId("cairn-2e.character.rolls"),
        prompt = "Roll character details",
        rolls = listOf(
            RollSpec("background", DiceExpression(1, 20)),
            RollSpec("str", DiceExpression(3, 6)),
            RollSpec("dex", DiceExpression(3, 6)),
            RollSpec("wil", DiceExpression(3, 6)),
            RollSpec("hp", DiceExpression(1, 6)),
            RollSpec("age", DiceExpression(2, 20, 10)),
            RollSpec("lifepath-past", DiceExpression(1, 6)),
            RollSpec("lifepath-present", DiceExpression(1, 6)),
        ),
    )

    override fun start(): InteractiveStep.Waiting = InteractiveStep.Waiting(
        CairnCreationState.AwaitingRolls(rollsRequest),
        rollsRequest,
    )

    override fun advance(state: ProcessState, response: ProcessResponse): InteractiveStep = when (state) {
        is CairnCreationState.AwaitingRolls -> acceptRolls(state, response)
        is CairnCreationState.AwaitingName -> acceptName(state, response)
        is CairnCreationState.AwaitingSwap -> acceptSwap(state, response)
        else -> throw IllegalArgumentException("State does not belong to ${id.value}")
    }

    private fun acceptRolls(state: CairnCreationState.AwaitingRolls, response: ProcessResponse): InteractiveStep.Waiting {
        state.request.requireMatching(response)
        require(response is ProcessResponse.Rolled) { "Roll response required" }
        val expected = state.request.rolls.map(RollSpec::id).toSet()
        require(response.totals.keys == expected) { "Roll response must contain exactly $expected" }
        state.request.rolls.forEach { spec ->
            val total = response.totals.getValue(spec.id)
            val range = (spec.dice.count + spec.dice.modifier)..(spec.dice.count * spec.dice.sides + spec.dice.modifier)
            require(total in range) { "${spec.id} result is outside $range" }
        }
        val background = CairnCharacterData.backgrounds[response.totals.getValue("background") - 1]
        val generated = Generated(
            background = background,
            attributes = listOf("str", "dex", "wil").map(response.totals::getValue),
            hitProtection = response.totals.getValue("hp"),
            age = response.totals.getValue("age"),
            lifepathRolls = listOf("lifepath-past", "lifepath-present").map(response.totals::getValue),
        )
        val request = ProcessRequest.Choose(
            RequestId("cairn-2e.character.name"),
            "Choose a name for ${background.name}",
            background.names.mapIndexed { index, name -> ChoiceOption(index.toString(), name) },
        )
        return InteractiveStep.Waiting(CairnCreationState.AwaitingName(generated, request), request)
    }

    private fun acceptName(state: CairnCreationState.AwaitingName, response: ProcessResponse): InteractiveStep.Waiting {
        state.request.requireMatching(response)
        require(response is ProcessResponse.Selected && response.optionIds.size == 1) { "One selected name is required" }
        val option = state.request.options.single { it.id == response.optionIds.single() }
        val source = CairnCharacterData.lifepaths.getValue(state.generated.background.lifepathId)
        val experiences = source.tables.zip(state.generated.lifepathRolls).map { (table, roll) ->
            LifepathExperience(table.prompt, roll, table.results.single { it.roll == roll }.text)
        }
        val request = ProcessRequest.Choose(
            RequestId("cairn-2e.character.attribute-swap"),
            "Keep attributes in order or swap one pair",
            listOf(
                ChoiceOption("keep", "Keep STR/DEX/WIL"),
                ChoiceOption("str-dex", "Swap STR and DEX"),
                ChoiceOption("str-wil", "Swap STR and WIL"),
                ChoiceOption("dex-wil", "Swap DEX and WIL"),
            ),
        )
        return InteractiveStep.Waiting(CairnCreationState.AwaitingSwap(state.generated, option.label, experiences, request), request)
    }

    private fun acceptSwap(state: CairnCreationState.AwaitingSwap, response: ProcessResponse): InteractiveStep.Completed {
        state.request.requireMatching(response)
        require(response is ProcessResponse.Selected && response.optionIds.size == 1) { "One swap choice is required" }
        val choice = response.optionIds.single()
        require(state.request.options.any { it.id == choice }) { "Unknown swap choice: $choice" }
        val scores = state.generated.attributes.toMutableList()
        val positions = when (choice) {
            "keep" -> null
            "str-dex" -> 0 to 1
            "str-wil" -> 0 to 2
            "dex-wil" -> 1 to 2
            else -> error("Validated above")
        }
        positions?.let { (first, second) ->
            val value = scores[first]
            scores[first] = scores[second]
            scores[second] = value
        }
        return InteractiveStep.Completed(
            CairnCharacter(
                name = state.name,
                age = state.generated.age,
                background = state.generated.background,
                attributes = Attribute.entries.zip(scores).toMap(),
                hitProtection = state.generated.hitProtection,
                lifepath = state.lifepath,
            ),
        )
    }
}
