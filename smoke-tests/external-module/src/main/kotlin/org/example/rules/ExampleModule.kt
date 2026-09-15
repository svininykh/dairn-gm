package org.example.rules

import org.dairn.core.ArtifactField
import org.dairn.core.InteractiveCharacterCreationModule
import org.dairn.core.InteractiveProcess
import org.dairn.core.InteractiveStep
import org.dairn.core.ModuleId
import org.dairn.core.ModuleInfo
import org.dairn.core.ProcessArtifact
import org.dairn.core.ProcessId
import org.dairn.core.ProcessRequest
import org.dairn.core.ProcessResponse
import org.dairn.core.ProcessState
import org.dairn.core.RequestId
import org.dairn.core.requireMatching

object ExampleModule : InteractiveCharacterCreationModule {
    override val info = ModuleInfo(ModuleId("example-rules"), "1.0.0", "Example Rules")

    override fun characterCreationProcess(languageTag: String): InteractiveProcess = ExampleCharacterCreation
}

private data class ExampleState(val request: ProcessRequest) : ProcessState

private object ExampleCharacterCreation : InteractiveProcess {
    override val id = ProcessId("example.character-creation")

    override fun start(): InteractiveStep.Waiting {
        val request = ProcessRequest.EnterText(RequestId("example.character.name"), "Character name")
        return InteractiveStep.Waiting(ExampleState(request), request)
    }

    override fun advance(state: ProcessState, response: ProcessResponse): InteractiveStep {
        require(state is ExampleState)
        state.request.requireMatching(response)
        require(response is ProcessResponse.TextEntered && response.value.isNotBlank())
        return InteractiveStep.Completed(
            object : ProcessArtifact {
                override val type = "example.character"
                override val fields = listOf(ArtifactField("Name", response.value))
            },
        )
    }
}
