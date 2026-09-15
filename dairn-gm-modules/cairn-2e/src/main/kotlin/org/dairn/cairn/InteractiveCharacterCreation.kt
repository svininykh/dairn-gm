package org.dairn.cairn

import org.dairn.core.*

data class CairnCharacter(
    val name: String,
    val age: Int,
    val background: CharacterBackground,
    val attributes: Map<Attribute, Int>,
    val hitProtection: Int,
    val goldPieces: Int,
    val backgroundResults: List<ResolvedBackgroundTable>,
    val traits: List<RolledCharacterTrait>,
    val bond: ResolvedBond,
    private val labels: Map<String, String>,
) : ProcessArtifact {
    override val type: String = "cairn-2e.character"
    override val fields: List<ArtifactField>
        get() = listOf(
            ArtifactField(labels.getValue("name"), name),
            ArtifactField(labels.getValue("age"), age),
            ArtifactField(labels.getValue("background"), background.name),
            ArtifactField(labels.getValue("str"), attributes.getValue(Attribute.STRENGTH)),
            ArtifactField(labels.getValue("dex"), attributes.getValue(Attribute.DEXTERITY)),
            ArtifactField(labels.getValue("wil"), attributes.getValue(Attribute.WILLPOWER)),
            ArtifactField(labels.getValue("hp"), hitProtection),
            ArtifactField(labels.getValue("gp"), goldPieces),
            ArtifactField(labels.getValue("equipment"), background.startingEquipment),
            ArtifactField(labels.getValue("background-results"), backgroundResults.map(ResolvedBackgroundTable::text)),
            *traits.map { ArtifactField(it.name, it.result) }.toTypedArray(),
            ArtifactField(labels.getValue("bond"), bond.text),
        )
}

private data class CairnDraft(
    val background: CharacterBackground? = null,
    val name: String? = null,
    val backgroundResults: List<ResolvedBackgroundTable> = emptyList(),
    val attributes: List<Int> = emptyList(),
    val hitProtection: Int? = null,
    val goldPieces: Int? = null,
    val traits: List<RolledCharacterTrait> = emptyList(),
    val bond: ResolvedBond? = null,
)

private sealed interface CairnCreationState : ProcessState {
    val request: ProcessRequest

    data class AwaitingBackground(
        override val request: ProcessRequest.Choose,
    ) : CairnCreationState

    data class AwaitingBackgroundRoll(
        override val request: ProcessRequest.Roll,
    ) : CairnCreationState

    data class AwaitingName(
        val draft: CairnDraft,
        override val request: ProcessRequest.Choose,
    ) : CairnCreationState

    data class AwaitingBackgroundTables(
        val draft: CairnDraft,
        override val request: ProcessRequest.Roll,
    ) : CairnCreationState

    data class AwaitingAbilities(
        val draft: CairnDraft,
        override val request: ProcessRequest.Roll,
    ) : CairnCreationState

    data class AwaitingSwap(
        val draft: CairnDraft,
        override val request: ProcessRequest.Choose,
    ) : CairnCreationState

    data class AwaitingTraitsAndBond(
        val draft: CairnDraft,
        override val request: ProcessRequest.Roll,
    ) : CairnCreationState

    data class AwaitingAge(
        val draft: CairnDraft,
        override val request: ProcessRequest.Roll,
    ) : CairnCreationState
}

class CairnInteractiveCharacterCreation(languageTag: String = "en") : InteractiveProcess {
    private val text = CairnText(languageTag)
    private val backgrounds = CairnCharacterData.backgrounds(languageTag)
    override val id = ProcessId("cairn-2e.character-creation")

    override fun start(): InteractiveStep.Waiting {
        val request = ProcessRequest.Choose(
            RequestId("cairn-2e.character.background"),
            text.get("process.background.prompt"),
            listOf(ChoiceOption("roll", text.get("process.background.roll"))) +
                backgrounds.map { ChoiceOption(it.id, it.name) },
        )
        return InteractiveStep.Waiting(CairnCreationState.AwaitingBackground(request), request)
    }

    override fun advance(state: ProcessState, response: ProcessResponse): InteractiveStep = when (state) {
        is CairnCreationState.AwaitingBackground -> acceptBackground(state, response)
        is CairnCreationState.AwaitingBackgroundRoll -> acceptBackgroundRoll(state, response)
        is CairnCreationState.AwaitingName -> acceptName(state, response)
        is CairnCreationState.AwaitingBackgroundTables -> acceptBackgroundTables(state, response)
        is CairnCreationState.AwaitingAbilities -> acceptAbilities(state, response)
        is CairnCreationState.AwaitingSwap -> acceptSwap(state, response)
        is CairnCreationState.AwaitingTraitsAndBond -> acceptTraitsAndBond(state, response)
        is CairnCreationState.AwaitingAge -> acceptAge(state, response)
        else -> throw IllegalArgumentException("State does not belong to ${id.value}")
    }

    private fun acceptBackground(
        state: CairnCreationState.AwaitingBackground,
        response: ProcessResponse,
    ): InteractiveStep.Waiting {
        val selected = selectedOption(state.request, response)
        if (selected == "roll") {
            val request = ProcessRequest.Roll(
                RequestId("cairn-2e.character.background-roll"),
                text.get("process.background-roll.prompt"),
                listOf(RollSpec("background", DiceExpression(1, 20))),
            )
            return InteractiveStep.Waiting(CairnCreationState.AwaitingBackgroundRoll(request), request)
        }
        return requestName(backgrounds.single { it.id == selected })
    }

    private fun acceptBackgroundRoll(
        state: CairnCreationState.AwaitingBackgroundRoll,
        response: ProcessResponse,
    ): InteractiveStep.Waiting {
        val totals = rolledTotals(state.request, response)
        return requestName(backgrounds[totals.getValue("background") - 1])
    }

    private fun requestName(background: CharacterBackground): InteractiveStep.Waiting {
        val request = ProcessRequest.Choose(
            RequestId("cairn-2e.character.name"),
            text.format("process.name.prompt", background.name),
            background.names.mapIndexed { index, name -> ChoiceOption(index.toString(), name) },
        )
        return InteractiveStep.Waiting(CairnCreationState.AwaitingName(CairnDraft(background = background), request), request)
    }

    private fun acceptName(
        state: CairnCreationState.AwaitingName,
        response: ProcessResponse,
    ): InteractiveStep.Waiting {
        val option = state.request.options.single { it.id == selectedOption(state.request, response) }
        val background = requireNotNull(state.draft.background)
        val request = ProcessRequest.Roll(
            RequestId("cairn-2e.character.background-tables"),
            text.format("process.background-tables.prompt", background.name),
            listOf(
                RollSpec("gold", DiceExpression(3, 6)),
                RollSpec("background-table-1", DiceExpression(1, 6)),
                RollSpec("background-table-2", DiceExpression(1, 6)),
            ),
        )
        return InteractiveStep.Waiting(
            CairnCreationState.AwaitingBackgroundTables(state.draft.copy(name = option.label), request),
            request,
        )
    }

    private fun acceptBackgroundTables(
        state: CairnCreationState.AwaitingBackgroundTables,
        response: ProcessResponse,
    ): InteractiveStep.Waiting {
        val totals = rolledTotals(state.request, response)
        val source = CairnCharacterData.backgroundTables.getValue(requireNotNull(state.draft.background).id)
        val results = source.tables.mapIndexed { index, table ->
            val roll = totals.getValue("background-table-${index + 1}")
            ResolvedBackgroundTable(
                text.get(table.promptKey),
                roll,
                text.get(table.results.single { it.roll == roll }.textKey),
            )
        }
        val request = ProcessRequest.Roll(
            RequestId("cairn-2e.character.abilities"),
            text.get("process.abilities.prompt"),
            listOf(
                RollSpec("str", DiceExpression(3, 6)),
                RollSpec("dex", DiceExpression(3, 6)),
                RollSpec("wil", DiceExpression(3, 6)),
                RollSpec("hp", DiceExpression(1, 6)),
            ),
        )
        return InteractiveStep.Waiting(
            CairnCreationState.AwaitingAbilities(
                state.draft.copy(backgroundResults = results, goldPieces = totals.getValue("gold")),
                request,
            ),
            request,
        )
    }

    private fun acceptAbilities(
        state: CairnCreationState.AwaitingAbilities,
        response: ProcessResponse,
    ): InteractiveStep.Waiting {
        val totals = rolledTotals(state.request, response)
        val request = ProcessRequest.Choose(
            RequestId("cairn-2e.character.attribute-swap"),
            text.get("process.swap.prompt"),
            listOf(
                ChoiceOption("keep", text.get("process.swap.keep")),
                ChoiceOption("str-dex", text.get("process.swap.str-dex")),
                ChoiceOption("str-wil", text.get("process.swap.str-wil")),
                ChoiceOption("dex-wil", text.get("process.swap.dex-wil")),
            ),
        )
        val draft = state.draft.copy(
            attributes = listOf("str", "dex", "wil").map(totals::getValue),
            hitProtection = totals.getValue("hp"),
        )
        return InteractiveStep.Waiting(CairnCreationState.AwaitingSwap(draft, request), request)
    }

    private fun acceptSwap(
        state: CairnCreationState.AwaitingSwap,
        response: ProcessResponse,
    ): InteractiveStep.Waiting {
        val choice = selectedOption(state.request, response)
        val scores = state.draft.attributes.toMutableList()
        when (choice) {
            "str-dex" -> scores.swap(0, 1)
            "str-wil" -> scores.swap(0, 2)
            "dex-wil" -> scores.swap(1, 2)
        }
        val request = ProcessRequest.Roll(
            RequestId("cairn-2e.character.traits-and-bond"),
            text.get("process.traits-bond.prompt"),
            CairnCharacterData.traits.map { RollSpec("trait-${it.id}", DiceExpression(1, 10)) } +
                RollSpec("bond", DiceExpression(1, 20)),
        )
        return InteractiveStep.Waiting(
            CairnCreationState.AwaitingTraitsAndBond(state.draft.copy(attributes = scores), request),
            request,
        )
    }

    private fun acceptTraitsAndBond(
        state: CairnCreationState.AwaitingTraitsAndBond,
        response: ProcessResponse,
    ): InteractiveStep.Waiting {
        val totals = rolledTotals(state.request, response)
        val traits = CairnCharacterData.traits.map { trait ->
            val roll = totals.getValue("trait-${trait.id}")
            RolledCharacterTrait(trait.id, text.get(trait.nameKey), roll, text.get(trait.resultKeys[roll - 1]))
        }
        val request = ProcessRequest.Roll(
            RequestId("cairn-2e.character.age"),
            text.get("process.age.prompt"),
            listOf(RollSpec("age", DiceExpression(2, 20, 10))),
        )
        val draft = state.draft.copy(
            traits = traits,
            bond = CairnCharacterData.bonds[totals.getValue("bond") - 1].let {
                ResolvedBond(it.roll, text.get(it.textKey))
            },
        )
        return InteractiveStep.Waiting(CairnCreationState.AwaitingAge(draft, request), request)
    }

    private fun acceptAge(
        state: CairnCreationState.AwaitingAge,
        response: ProcessResponse,
    ): InteractiveStep.Completed {
        val age = rolledTotals(state.request, response).getValue("age")
        return InteractiveStep.Completed(
            CairnCharacter(
                name = requireNotNull(state.draft.name),
                age = age,
                background = requireNotNull(state.draft.background),
                attributes = Attribute.entries.zip(state.draft.attributes).toMap(),
                hitProtection = requireNotNull(state.draft.hitProtection),
                goldPieces = requireNotNull(state.draft.goldPieces),
                backgroundResults = state.draft.backgroundResults,
                traits = state.draft.traits,
                bond = requireNotNull(state.draft.bond),
                labels = listOf(
                    "name", "age", "background", "str", "dex", "wil", "hp", "gp",
                    "equipment", "background-results", "bond",
                ).associateWith { text.get("field.$it") },
            ),
        )
    }

    private fun selectedOption(request: ProcessRequest.Choose, response: ProcessResponse): String {
        request.requireMatching(response)
        require(response is ProcessResponse.Selected && response.optionIds.size == 1) { "One selection is required" }
        return response.optionIds.single().also { selected ->
            require(request.options.any { it.id == selected }) { "Unknown choice: $selected" }
        }
    }

    private fun rolledTotals(request: ProcessRequest.Roll, response: ProcessResponse): Map<String, Int> {
        request.requireMatching(response)
        require(response is ProcessResponse.Rolled) { "Roll response required" }
        val expected = request.rolls.map(RollSpec::id).toSet()
        require(response.totals.keys == expected) { "Roll response must contain exactly $expected" }
        request.rolls.forEach { spec ->
            val total = response.totals.getValue(spec.id)
            val range = (spec.dice.count + spec.dice.modifier)..(spec.dice.count * spec.dice.sides + spec.dice.modifier)
            require(total in range) { "${spec.id} result is outside $range" }
        }
        return response.totals
    }

    private fun <T> MutableList<T>.swap(first: Int, second: Int) {
        val value = this[first]
        this[first] = this[second]
        this[second] = value
    }
}
