package io.github.dairn.cairn

import io.github.dairn.core.InteractiveStep
import io.github.dairn.core.ProcessRequest
import io.github.dairn.core.ProcessResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InteractiveCharacterCreationTest {
    @Test
    fun `resource catalog contains complete backgrounds and their tables`() {
        assertEquals(20, CairnCharacterData.backgrounds.size)
        assertEquals(20, CairnCharacterData.backgroundTables.size)
        assertEquals(8, CairnCharacterData.traits.size)
        assertEquals((1..20).toList(), CairnCharacterData.bonds.map(Bond::roll))
        assertEquals((1..20).toList(), CairnCharacterData.omens.map(Omen::roll))
        CairnCharacterData.traits.forEach { assertEquals(10, it.resultKeys.size, it.id) }
        CairnCharacterData.backgrounds.forEach { background ->
            assertEquals(10, background.names.size, background.name)
            assertEquals(2, CairnCharacterData.backgroundTables.getValue(background.id).tables.size)
        }
    }

    @Test
    fun `process follows the second edition creation order`() {
        val process = CairnInteractiveCharacterCreation()
        var waiting = process.start()
        val backgroundChoice = assertIs<ProcessRequest.Choose>(waiting.request)
        assertEquals("roll", backgroundChoice.options.first().id)
        assertEquals(21, backgroundChoice.options.size)

        waiting = assertIs(
            process.advance(
                waiting.state,
                ProcessResponse.Selected(backgroundChoice.id, listOf("roll")),
            ),
        )
        val backgroundRoll = assertIs<ProcessRequest.Roll>(waiting.request)
        waiting = assertIs(
            process.advance(
                waiting.state,
                ProcessResponse.Rolled(backgroundRoll.id, mapOf("background" to 1)),
            ),
        )
        val names = assertIs<ProcessRequest.Choose>(waiting.request)
        assertEquals("Hestia", names.options.first().label)

        waiting = assertIs(
            process.advance(
                waiting.state,
                ProcessResponse.Selected(names.id, listOf("1")),
            ),
        )
        val backgroundTables = assertIs<ProcessRequest.Roll>(waiting.request)
        waiting = assertIs(
            process.advance(
                waiting.state,
                ProcessResponse.Rolled(
                    backgroundTables.id,
                    mapOf("gold" to 11, "background-table-1" to 1, "background-table-2" to 6),
                ),
            ),
        )
        val abilities = assertIs<ProcessRequest.Roll>(waiting.request)
        assertEquals(listOf("str", "dex", "wil", "hp"), abilities.rolls.map { it.id })
        waiting = assertIs(
            process.advance(
                waiting.state,
                ProcessResponse.Rolled(
                    abilities.id,
                    mapOf("str" to 8, "dex" to 12, "wil" to 15, "hp" to 4),
                ),
            ),
        )
        val swap = assertIs<ProcessRequest.Choose>(waiting.request)
        waiting = assertIs(
            process.advance(
                waiting.state,
                ProcessResponse.Selected(swap.id, listOf("str-wil")),
            ),
        )
        val traitsAndBond = assertIs<ProcessRequest.Roll>(waiting.request)
        waiting = assertIs(
            process.advance(
                waiting.state,
                ProcessResponse.Rolled(
                    traitsAndBond.id,
                    mapOf(
                        "trait-physique" to 1,
                        "trait-skin" to 2,
                        "trait-hair" to 3,
                        "trait-face" to 4,
                        "trait-speech" to 5,
                        "trait-clothing" to 6,
                        "trait-virtue" to 7,
                        "trait-vice" to 8,
                        "bond" to 1,
                    ),
                ),
            ),
        )
        val age = assertIs<ProcessRequest.Roll>(waiting.request)
        val completed = assertIs<InteractiveStep.Completed>(
            process.advance(
                waiting.state,
                ProcessResponse.Rolled(age.id, mapOf("age" to 27)),
            ),
        )
        val character = assertIs<CairnCharacter>(completed.artifact)
        assertEquals("Basil", character.name)
        assertEquals(11, character.goldPieces)
        assertEquals(listOf(15, 12, 8), Attribute.entries.map(character.attributes::getValue))
        assertEquals(listOf(1, 6), character.backgroundResults.map { it.roll })
        assertEquals(
            listOf("Athletic", "Marked", "Curly", "Elongated", "Formal", "Frayed", "Humble", "Rude"),
            character.traits.map { it.result },
        )
        assertEquals(1, character.bond.roll)
    }

    @Test
    fun `background can be selected without a roll`() {
        val process = CairnInteractiveCharacterCreation()
        val started = process.start()
        val request = assertIs<ProcessRequest.Choose>(started.request)
        val next = assertIs<InteractiveStep.Waiting>(
            process.advance(
                started.state,
                ProcessResponse.Selected(request.id, listOf("foundling")),
            ),
        )
        assertIs<ProcessRequest.Choose>(next.request)
        assertEquals("Choose a name for Foundling", next.request.prompt)
    }

    @Test
    fun `official Russian bundle localizes backgrounds and their tables`() {
        val process = CairnInteractiveCharacterCreation("ru")
        val started = process.start()
        val backgrounds = assertIs<ProcessRequest.Choose>(started.request)
        assertEquals("Ремесленник", backgrounds.options.single { it.id == "aurifex" }.label)

        val namesStep = assertIs<InteractiveStep.Waiting>(
            process.advance(started.state, ProcessResponse.Selected(backgrounds.id, listOf("aurifex"))),
        )
        val names = assertIs<ProcessRequest.Choose>(namesStep.request)
        assertEquals("Гестия", names.options.first().label)
    }
}
