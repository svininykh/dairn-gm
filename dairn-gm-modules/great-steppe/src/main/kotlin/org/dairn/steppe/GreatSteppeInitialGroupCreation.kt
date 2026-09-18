package org.dairn.steppe

import org.dairn.core.*

data class GreatSteppeInitialGroup(
    val characters: List<GreatSteppeCharacter>,
    val youngest: GreatSteppeCharacter,
    val omen: GreatSteppeOmen,
    private val labels: Map<String, String>,
) : ProcessArtifact {
    private fun memberLabel(character: GreatSteppeCharacter, index: Int): String =
        character.name ?: labels.getValue("unnamed-member").format(index)

    override val type = "great-steppe.initial-group"
    override val fields: List<ArtifactField> = listOf(
        ArtifactField(
            labels.getValue("members"),
            characters.mapIndexed { index, character -> "${memberLabel(character, index + 1)} (${character.age}) — ${character.lifePath.name}" },
        ),
        ArtifactField(labels.getValue("youngest"), memberLabel(youngest, characters.indexOf(youngest) + 1)),
        ArtifactField(labels.getValue("omen"), listOf(omen.name, omen.description)),
    )
}

private enum class InitialGroupStage { CHARACTER, YOUNGEST, OMEN }

private data class InitialGroupState(
    val stage: InitialGroupStage,
    val characters: List<GreatSteppeCharacter>,
    val memberNumber: Int,
    val childState: ProcessState?,
    val childRequest: ProcessRequest?,
    val youngest: GreatSteppeCharacter?,
    val request: ProcessRequest,
) : ProcessState

class GreatSteppeInitialGroupCreation(
    private val memberCount: Int,
    languageTag: String = "ru",
) : InteractiveProcess {
    override val id = ProcessId("great-steppe.initial-group-creation")
    private val text = GreatSteppeText(languageTag)
    private val characterProcess = GreatSteppeCharacterCreation(languageTag)

    init {
        require(memberCount > 0) { "An initial group must contain at least one character" }
    }

    override fun start(): InteractiveStep.Waiting = startCharacter(emptyList(), memberNumber = 1)

    override fun advance(state: ProcessState, response: ProcessResponse): InteractiveStep {
        require(state is InitialGroupState) { "State does not belong to ${id.value}" }
        state.request.requireMatching(response)
        return when (state.stage) {
            InitialGroupStage.CHARACTER -> advanceCharacter(state, response)
            InitialGroupStage.YOUNGEST -> acceptYoungest(state, response)
            InitialGroupStage.OMEN -> acceptOmen(state, response)
        }
    }

    private fun advanceCharacter(state: InitialGroupState, response: ProcessResponse): InteractiveStep {
        val childRequest = requireNotNull(state.childRequest)
        val childStep = characterProcess.advance(
            requireNotNull(state.childState),
            response.forRequest(childRequest.id),
        )
        return when (childStep) {
            is InteractiveStep.Waiting -> waitingForCharacter(
                state.characters,
                state.memberNumber,
                childStep,
            )
            is InteractiveStep.Completed -> {
                val character = childStep.artifact as GreatSteppeCharacter
                val completed = state.characters + character
                if (completed.size < memberCount) {
                    startCharacter(completed, state.memberNumber + 1)
                } else {
                    requestGroupOmen(completed)
                }
            }
        }
    }

    private fun startCharacter(
        characters: List<GreatSteppeCharacter>,
        memberNumber: Int,
    ): InteractiveStep.Waiting = waitingForCharacter(characters, memberNumber, characterProcess.start())

    private fun waitingForCharacter(
        characters: List<GreatSteppeCharacter>,
        memberNumber: Int,
        child: InteractiveStep.Waiting,
    ): InteractiveStep.Waiting {
        val request = child.request.forMember(memberNumber)
        return InteractiveStep.Waiting(
            InitialGroupState(
                InitialGroupStage.CHARACTER,
                characters,
                memberNumber,
                child.state,
                child.request,
                null,
                request,
            ),
            request,
        )
    }

    private fun requestGroupOmen(characters: List<GreatSteppeCharacter>): InteractiveStep.Waiting {
        val minimumAge = characters.minOf(GreatSteppeCharacter::age)
        val youngest = characters.withIndex().filter { it.value.age == minimumAge }
        return if (youngest.size == 1) {
            requestOmen(characters, youngest.single().value)
        } else {
            val request = ProcessRequest.Choose(
                RequestId("great-steppe.initial-group.youngest"),
                text.get("process.initial-group.youngest.prompt"),
                youngest.map { candidate ->
                    ChoiceOption(
                        "member-${candidate.index + 1}",
                        "${candidate.value.name ?: text.get("process.initial-group.member.unnamed").format(candidate.index + 1)} (${candidate.value.age})",
                    )
                },
            )
            InteractiveStep.Waiting(
                InitialGroupState(
                    InitialGroupStage.YOUNGEST,
                    characters,
                    memberCount,
                    null,
                    null,
                    null,
                    request,
                ),
                request,
            )
        }
    }

    private fun acceptYoungest(
        state: InitialGroupState,
        response: ProcessResponse,
    ): InteractiveStep.Waiting {
        require(response is ProcessResponse.Selected && response.optionIds.size == 1)
        val selected = response.optionIds.single()
        val request = state.request as ProcessRequest.Choose
        require(request.options.any { it.id == selected })
        val index = selected.removePrefix("member-").toInt() - 1
        return requestOmen(state.characters, state.characters[index])
    }

    private fun requestOmen(
        characters: List<GreatSteppeCharacter>,
        youngest: GreatSteppeCharacter,
    ): InteractiveStep.Waiting {
        val request = ProcessRequest.Roll(
            RequestId("great-steppe.initial-group.omen"),
            text.get("process.group.omen.prompt"),
            listOf(RollSpec("omen", DiceExpression(1, 20))),
        )
        return InteractiveStep.Waiting(
            InitialGroupState(
                InitialGroupStage.OMEN,
                characters,
                memberCount,
                null,
                null,
                youngest,
                request,
            ),
            request,
        )
    }

    private fun acceptOmen(
        state: InitialGroupState,
        response: ProcessResponse,
    ): InteractiveStep.Completed {
        require(response is ProcessResponse.Rolled && response.totals.keys == setOf("omen"))
        val roll = response.totals.getValue("omen")
        require(roll in 1..20)
        return InteractiveStep.Completed(
            GreatSteppeInitialGroup(
                state.characters,
                requireNotNull(state.youngest),
                GreatSteppeModule.resolveOmen(roll, text.languageTag),
                mapOf(
                    "members" to text.get("field.group-members"),
                    "youngest" to text.get("field.youngest-character"),
                    "omen" to text.get("field.group-omen"),
                    "unnamed-member" to text.get("process.initial-group.member.unnamed"),
                ),
            ),
        )
    }

    private fun ProcessRequest.forMember(memberNumber: Int): ProcessRequest {
        val suffix = id.value.removePrefix("great-steppe.character.")
        val externalId = RequestId("great-steppe.initial-group.member-$memberNumber.$suffix")
        val externalPrompt = text.get("process.initial-group.member.prompt")
            .format(memberNumber, memberCount, prompt)
        return when (this) {
            is ProcessRequest.Roll -> copy(id = externalId, prompt = externalPrompt)
            is ProcessRequest.Choose -> copy(id = externalId, prompt = externalPrompt)
            is ProcessRequest.EnterText -> copy(id = externalId, prompt = externalPrompt)
        }
    }

    private fun ProcessResponse.forRequest(internalId: RequestId): ProcessResponse = when (this) {
        is ProcessResponse.Rolled -> copy(requestId = internalId)
        is ProcessResponse.Selected -> copy(requestId = internalId)
        is ProcessResponse.TextEntered -> copy(requestId = internalId)
    }
}
