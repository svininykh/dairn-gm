package org.dairn.core

/**
 * An immutable, module-owned snapshot of an unfinished interactive process.
 *
 * Hosts must treat the state as opaque and return it to the same process implementation.
 * Persistence and serialization are not part of the Engine 0.1 contract.
 */
interface ProcessState

@JvmInline
value class ProcessId(val value: String) {
    init {
        require(value.matches(Regex("[a-z0-9]+(?:[.-][a-z0-9]+)*"))) { "Invalid process id: $value" }
    }
}

@JvmInline
value class RequestId(val value: String) {
    init {
        require(value.matches(Regex("[a-z0-9]+(?:[.-][a-z0-9]+)*"))) { "Invalid request id: $value" }
    }
}

data class DiceExpression(
    val count: Int,
    val sides: Int,
    val modifier: Int = 0,
) {
    init {
        require(count > 0) { "Dice count must be positive" }
        require(sides > 1) { "A die must have at least two sides" }
    }
}

data class RollSpec(
    val id: String,
    val dice: DiceExpression,
) {
    init {
        require(id.isNotBlank()) { "Roll id cannot be blank" }
    }
}

data class ChoiceOption(
    val id: String,
    val label: String,
) {
    init {
        require(id.isNotBlank()) { "Choice option id cannot be blank" }
        require(label.isNotBlank()) { "Choice option label cannot be blank" }
    }
}

sealed interface ProcessRequest {
    val id: RequestId
    val prompt: String

    data class Roll(
        override val id: RequestId,
        override val prompt: String,
        val rolls: List<RollSpec>,
    ) : ProcessRequest {
        init {
            require(prompt.isNotBlank())
            require(rolls.isNotEmpty())
            require(rolls.map(RollSpec::id).distinct().size == rolls.size) { "Roll ids must be unique" }
        }
    }

    data class Choose(
        override val id: RequestId,
        override val prompt: String,
        val options: List<ChoiceOption>,
        val minimum: Int = 1,
        val maximum: Int = 1,
    ) : ProcessRequest {
        init {
            require(prompt.isNotBlank())
            require(options.isNotEmpty())
            require(options.map(ChoiceOption::id).distinct().size == options.size) { "Choice option ids must be unique" }
            require(minimum in 0..options.size)
            require(maximum in minimum..options.size)
        }
    }

    data class EnterText(
        override val id: RequestId,
        override val prompt: String,
        val allowBlank: Boolean = false,
    ) : ProcessRequest {
        init {
            require(prompt.isNotBlank())
        }
    }
}

sealed interface ProcessResponse {
    val requestId: RequestId

    data class Rolled(
        override val requestId: RequestId,
        val totals: Map<String, Int>,
    ) : ProcessResponse

    data class Selected(
        override val requestId: RequestId,
        val optionIds: List<String>,
    ) : ProcessResponse

    data class TextEntered(
        override val requestId: RequestId,
        val value: String,
    ) : ProcessResponse
}

interface ProcessArtifact {
    val type: String
    val fields: List<ArtifactField>
}

data class ArtifactField(
    val label: String,
    val values: List<String>,
) {
    constructor(label: String, value: Any) : this(label, listOf(value.toString()))

    init {
        require(label.isNotBlank())
        require(values.isNotEmpty())
    }
}

sealed interface InteractiveStep {
    data class Waiting(
        val state: ProcessState,
        val request: ProcessRequest,
    ) : InteractiveStep

    data class Completed(val artifact: ProcessArtifact) : InteractiveStep
}

/** Ruleset-neutral, stateless protocol used by any interactive shell. */
interface InteractiveProcess {
    val id: ProcessId

    fun start(): InteractiveStep.Waiting

    fun advance(state: ProcessState, response: ProcessResponse): InteractiveStep
}

/** Optional module capability for a ruleset-defined character creation process. */
interface InteractiveCharacterCreationModule : DairnModule {
    fun characterCreationProcess(languageTag: String = "en"): InteractiveProcess
}

data class GroupMemberInput(val id: String, val name: String, val age: Int) {
    init {
        require(id.isNotBlank()) { "Group member id cannot be blank" }
        require(name.isNotBlank()) { "Group member name cannot be blank" }
        require(age > 0) { "Group member age must be positive" }
    }
}

/** Optional module capability for creating a group from completed characters. */
interface InteractiveGroupCreationModule : DairnModule {
    fun groupCreationProcess(
        members: List<GroupMemberInput>,
        languageTag: String = "en",
    ): InteractiveProcess
}

/** Optional module capability for creating new characters and assembling their initial group. */
interface InteractiveInitialGroupCreationModule : DairnModule {
    fun initialGroupCreationProcess(
        memberCount: Int,
        languageTag: String = "en",
    ): InteractiveProcess
}

fun ProcessRequest.requireMatching(response: ProcessResponse) {
    require(id == response.requestId) {
        "Response ${response.requestId.value} does not match request ${id.value}"
    }
}
