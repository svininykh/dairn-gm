package io.github.dairn.steppe

import io.github.dairn.core.*

data class GreatSteppeGroup(
    val members: List<GroupMemberInput>,
    val youngest: GroupMemberInput,
    val omen: GreatSteppeOmen,
    private val labels: Map<String, String>,
) : ProcessArtifact {
    override val type = "great-steppe.group"
    override val fields: List<ArtifactField> = listOf(
        ArtifactField(labels.getValue("members"), members.map { "${it.name} (${it.age})" }),
        ArtifactField(labels.getValue("youngest"), youngest.name),
        ArtifactField(labels.getValue("omen"), listOf(omen.name, omen.description)),
    )
}

private data class GroupState(
    val youngest: GroupMemberInput?,
    val request: ProcessRequest,
) : ProcessState

class GreatSteppeGroupCreation(
    private val members: List<GroupMemberInput>,
    languageTag: String = "ru",
) : InteractiveProcess {
    override val id = ProcessId("great-steppe.group-creation")
    private val text = GreatSteppeText(languageTag)

    init {
        require(members.isNotEmpty()) { "A group must contain at least one member" }
        require(members.map(GroupMemberInput::id).distinct().size == members.size) {
            "Group member ids must be unique"
        }
    }

    override fun start(): InteractiveStep.Waiting {
        val minimumAge = members.minOf(GroupMemberInput::age)
        val youngest = members.filter { it.age == minimumAge }
        return if (youngest.size == 1) requestOmen(youngest.single()) else {
            val request = ProcessRequest.Choose(
                RequestId("great-steppe.group.youngest"),
                text.get("process.group.youngest.prompt"),
                youngest.map { ChoiceOption(it.id, "${it.name} (${it.age})") },
            )
            InteractiveStep.Waiting(GroupState(null, request), request)
        }
    }

    override fun advance(state: ProcessState, response: ProcessResponse): InteractiveStep {
        require(state is GroupState) { "State does not belong to ${id.value}" }
        state.request.requireMatching(response)
        return when (val request = state.request) {
            is ProcessRequest.Choose -> {
                require(response is ProcessResponse.Selected && response.optionIds.size == 1)
                val selected = response.optionIds.single()
                require(request.options.any { it.id == selected })
                requestOmen(members.single { it.id == selected })
            }
            is ProcessRequest.Roll -> {
                require(response is ProcessResponse.Rolled)
                require(response.totals.keys == setOf("omen"))
                val roll = response.totals.getValue("omen")
                require(roll in 1..20)
                val definition = GreatSteppeCharacterData.omens[roll - 1]
                val omen = GreatSteppeOmen(
                    roll,
                    text.get(definition.nameKey),
                    text.get(definition.descriptionKey),
                )
                InteractiveStep.Completed(
                    GreatSteppeGroup(
                        members,
                        requireNotNull(state.youngest),
                        omen,
                        mapOf(
                            "members" to text.get("field.group-members"),
                            "youngest" to text.get("field.youngest-character"),
                            "omen" to text.get("field.group-omen"),
                        ),
                    ),
                )
            }
            is ProcessRequest.EnterText -> error("Unexpected request")
        }
    }

    private fun requestOmen(youngest: GroupMemberInput): InteractiveStep.Waiting {
        val request = ProcessRequest.Roll(
            RequestId("great-steppe.group.omen"),
            text.get("process.group.omen.prompt"),
            listOf(RollSpec("omen", DiceExpression(1, 20))),
        )
        return InteractiveStep.Waiting(GroupState(youngest, request), request)
    }
}
