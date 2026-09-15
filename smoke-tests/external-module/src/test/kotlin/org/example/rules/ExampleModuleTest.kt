package org.example.rules

import org.dairn.core.InteractiveStep
import org.dairn.core.ProcessRequest
import org.dairn.core.ProcessResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ExampleModuleTest {
    @Test
    fun `external module compiles and runs against the published Engine artifact`() {
        val process = ExampleModule.characterCreationProcess("en")
        val waiting = process.start()
        val request = assertIs<ProcessRequest.EnterText>(waiting.request)

        val completed = process.advance(
            waiting.state,
            ProcessResponse.TextEntered(request.id, "Aster"),
        )

        assertEquals("example.character", assertIs<InteractiveStep.Completed>(completed).artifact.type)
    }
}
