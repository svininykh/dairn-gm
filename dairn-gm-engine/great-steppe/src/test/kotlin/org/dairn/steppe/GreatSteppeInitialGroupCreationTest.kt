package org.dairn.steppe

import org.dairn.core.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GreatSteppeInitialGroupCreationTest {
    @Test
    fun `one character can form an initial group`() {
        val artifact = complete(GreatSteppeInitialGroupCreation(1, "ru"))
        assertEquals(1, artifact.characters.size)
        assertEquals(artifact.characters.single(), artifact.youngest)
        assertEquals("great-steppe.initial-group", artifact.type)
    }

    @Test
    fun `member requests are namespaced and tied ages require a choice`() {
        val requestIds = mutableListOf<String>()
        val artifact = complete(GreatSteppeInitialGroupCreation(2, "ru"), requestIds)
        assertEquals(2, artifact.characters.size)
        assertNotEquals(artifact.characters[0].name, artifact.characters[1].name)
        assertEquals(artifact.characters[1], artifact.youngest)
        assertTrue(requestIds.any { it.startsWith("great-steppe.initial-group.member-1.") })
        assertTrue(requestIds.any { it.startsWith("great-steppe.initial-group.member-2.") })
        assertTrue("great-steppe.initial-group.youngest" in requestIds)
    }

    private fun complete(
        process: GreatSteppeInitialGroupCreation,
        requestIds: MutableList<String> = mutableListOf(),
    ): GreatSteppeInitialGroup {
        var step: InteractiveStep = process.start()
        while (step is InteractiveStep.Waiting) {
            val waiting = step
            val request = waiting.request
            requestIds += request.id.value
            val response = when (request) {
                is ProcessRequest.Roll -> ProcessResponse.Rolled(
                    request.id,
                    request.rolls.associate { it.id to (it.dice.count + it.dice.modifier) },
                )
                is ProcessRequest.EnterText -> ProcessResponse.TextEntered(
                    request.id,
                    if ("member-1" in request.id.value) "Aibek" else "Bayan",
                )
                is ProcessRequest.Choose -> ProcessResponse.Selected(
                    request.id,
                    listOf(
                        when {
                            request.id.value.endsWith(".life-path") -> "1"
                            request.id.value.endsWith(".supplies-swap") -> "keep"
                            request.id.value.endsWith(".attribute-swap") -> "keep"
                            request.id.value.endsWith(".youngest") -> "member-2"
                            else -> error("Unexpected choice: ${request.id.value}")
                        },
                    ),
                )
            }
            step = process.advance(waiting.state, response)
        }
        return assertIs<GreatSteppeInitialGroup>(assertIs<InteractiveStep.Completed>(step).artifact)
    }
}
