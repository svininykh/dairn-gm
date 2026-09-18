package org.dairn.steppe

import org.dairn.core.InteractiveStep
import org.dairn.core.InteractiveProcess
import org.dairn.core.ProcessRequest
import org.dairn.core.ProcessResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GreatSteppeCharacterCreationTest {
    @Test
    fun `foundling creation resolves canonical tables and a personal omen`() {
        val process = GreatSteppeCharacterCreation("ru")
        var step: InteractiveStep = process.start()
        step = choose(process, step, "18")
        step = enter(process, step, "Aibek")
        step = roll(process, step, mapOf(
            "water" to 1, "food" to 2, "fire" to 3,
            "weapon" to 4, "travel-gear" to 5, "tool" to 6,
        ))
        step = choose(process, step, "water-fire")
        step = roll(process, step, mapOf("str" to 8, "dex" to 12, "wil" to 10, "hp" to 4))
        step = choose(process, step, "str-wil")
        step = rollTraitsAndBond(process, step, listOf(1, 2, 3, 4, 5, 6, 7, 8), 9)
        step = roll(process, step, mapOf("age" to 25))
        step = roll(process, step, mapOf("omen" to 1))

        val completed = assertIs<InteractiveStep.Completed>(step)
        val character = assertIs<GreatSteppeCharacter>(completed.artifact)
        assertEquals(listOf(10, 12, 8), character.attributes)
        assertEquals("Aibek", character.name)
        assertEquals(18, character.lifePath.roll)
        assertEquals(25, character.age)
        assertEquals(1, assertNotNull(character.secretOmen).roll)
        assertEquals(5, character.inventory.load.occupiedSlots)
        assertEquals(5, character.inventory.load.freeSlots)
        assertEquals(
            GreatSteppeSupply(2, GreatSteppeSupplyUnit.DAYS),
            character.inventory[GreatSteppeInventoryCategory.WATER].supply,
        )
        assertEquals(1, character.inventory[GreatSteppeInventoryCategory.WATER].slots)
        assertEquals(
            GreatSteppeSupply(0, GreatSteppeSupplyUnit.NIGHTS),
            character.inventory[GreatSteppeInventoryCategory.FIRE].supply,
        )
        assertEquals(0, character.inventory[GreatSteppeInventoryCategory.FIRE].slots)
        assertFalse(character.fields.any { it.label == GreatSteppeText("ru").get("field.group-omen") })
    }

    @Test
    fun `non-foundling creation completes without a personal omen`() {
        val process = GreatSteppeCharacterCreation("ru")
        var step: InteractiveStep = process.start()
        step = choose(process, step, "1")
        step = enter(process, step, "Bayan")
        step = roll(process, step, mapOf(
            "water" to 1, "food" to 1, "fire" to 1,
            "weapon" to 1, "travel-gear" to 1, "tool" to 1,
        ))
        step = choose(process, step, "keep")
        step = roll(process, step, mapOf("str" to 9, "dex" to 10, "wil" to 11, "hp" to 3))
        step = choose(process, step, "keep")
        step = rollTraitsAndBond(process, step, List(8) { 1 }, 1)
        step = roll(process, step, mapOf("age" to 30))

        val character = assertIs<GreatSteppeCharacter>(assertIs<InteractiveStep.Completed>(step).artifact)
        assertEquals(null, character.secretOmen)
        assertEquals(6, character.inventory.items.size)
        assertEquals(5, character.inventory.load.occupiedSlots)
        assertEquals(2, character.inventory[GreatSteppeInventoryCategory.WEAPON].slots)
        assertEquals(true, character.inventory[GreatSteppeInventoryCategory.WEAPON].bulky)
    }

    @Test
    fun `name may be left for a later reader interaction`() {
        val process = GreatSteppeCharacterCreation("ru")
        var step: InteractiveStep = process.start()
        step = choose(process, step, "1")
        step = enter(process, step, "")
        step = roll(process, step, mapOf(
            "water" to 1, "food" to 1, "fire" to 1,
            "weapon" to 1, "travel-gear" to 1, "tool" to 1,
        ))
        step = choose(process, step, "keep")
        step = roll(process, step, mapOf("str" to 9, "dex" to 10, "wil" to 11, "hp" to 3))
        step = choose(process, step, "keep")
        step = rollTraitsAndBond(process, step, List(8) { 1 }, 1)
        step = roll(process, step, mapOf("age" to 30))

        val character = assertIs<GreatSteppeCharacter>(assertIs<InteractiveStep.Completed>(step).artifact)
        assertNull(character.name)
        assertFalse(character.fields.any { it.label == GreatSteppeText("ru").get("field.name") })
    }

    @Test
    fun `only craft and hidden-past life paths request an experience detail`() {
        val craft = GreatSteppeCharacterCreation("ru")
        var step: InteractiveStep = craft.start()
        step = choose(craft, step, "8")
        step = enter(craft, step, "Bayan")
        step = roll(craft, step, mapOf(
            "water" to 1, "food" to 1, "fire" to 1,
            "weapon" to 1, "travel-gear" to 1, "tool" to 1,
        ))
        step = choose(craft, step, "keep")

        val waiting = assertIs<InteractiveStep.Waiting>(step)
        val request = assertIs<ProcessRequest.EnterText>(waiting.request)
        assertEquals("great-steppe.character.experience-detail", request.id.value)
        assertEquals("Выберите ремесло, которому обучался персонаж", request.prompt)
    }

    private fun choose(process: InteractiveProcess, step: InteractiveStep, option: String): InteractiveStep {
        val waiting = assertIs<InteractiveStep.Waiting>(step)
        val request = assertIs<ProcessRequest.Choose>(waiting.request)
        return process.advance(waiting.state, ProcessResponse.Selected(request.id, listOf(option)))
    }

    private fun enter(process: InteractiveProcess, step: InteractiveStep, value: String): InteractiveStep {
        val waiting = assertIs<InteractiveStep.Waiting>(step)
        val request = assertIs<ProcessRequest.EnterText>(waiting.request)
        return process.advance(waiting.state, ProcessResponse.TextEntered(request.id, value))
    }

    private fun roll(process: InteractiveProcess, step: InteractiveStep, totals: Map<String, Int>): InteractiveStep {
        val waiting = assertIs<InteractiveStep.Waiting>(step)
        val request = assertIs<ProcessRequest.Roll>(waiting.request)
        return process.advance(waiting.state, ProcessResponse.Rolled(request.id, totals))
    }

    private fun rollTraitsAndBond(process: InteractiveProcess, initial: InteractiveStep, traitRolls: List<Int>, bondRoll: Int): InteractiveStep {
        var step = initial
        traitRolls.forEach { traitRoll ->
            step = choose(process, step, "roll")
            step = roll(process, step, mapOf("trait" to traitRoll))
        }
        step = choose(process, step, "roll")
        return roll(process, step, mapOf("bond" to bondRoll))
    }
}
