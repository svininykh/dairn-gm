package org.dairn.steppe

import org.dairn.core.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GreatSteppeGroupCreationTest {
    @Test
    fun `youngest character determines the common omen`() {
        val process = GreatSteppeGroupCreation(
            listOf(
                GroupMemberInput("aibek", "Aibek", 25),
                GroupMemberInput("bayan", "Bayan", 31),
            ),
            "ru",
        )
        val waiting = process.start()
        val request = assertIs<ProcessRequest.Roll>(waiting.request)
        val completed = assertIs<InteractiveStep.Completed>(
            process.advance(waiting.state, ProcessResponse.Rolled(request.id, mapOf("omen" to 4))),
        )
        val group = assertIs<GreatSteppeGroup>(completed.artifact)
        assertEquals("Aibek", group.youngest.name)
        assertEquals(4, group.omen.roll)
        assertEquals("great-steppe.group", group.type)
    }

    @Test
    fun `equal youngest ages require an explicit choice`() {
        val process = GreatSteppeGroupCreation(
            listOf(
                GroupMemberInput("aibek", "Aibek", 25),
                GroupMemberInput("bayan", "Bayan", 25),
                GroupMemberInput("dana", "Dana", 30),
            ),
            "ru",
        )
        val choiceWaiting = process.start()
        val choice = assertIs<ProcessRequest.Choose>(choiceWaiting.request)
        assertEquals(listOf("aibek", "bayan"), choice.options.map(ChoiceOption::id))
        val rollWaiting = assertIs<InteractiveStep.Waiting>(
            process.advance(choiceWaiting.state, ProcessResponse.Selected(choice.id, listOf("bayan"))),
        )
        val roll = assertIs<ProcessRequest.Roll>(rollWaiting.request)
        val completed = assertIs<InteractiveStep.Completed>(
            process.advance(rollWaiting.state, ProcessResponse.Rolled(roll.id, mapOf("omen" to 1))),
        )
        assertEquals("Bayan", assertIs<GreatSteppeGroup>(completed.artifact).youngest.name)
    }
}
