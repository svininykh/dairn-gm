package org.example.storyteller

import org.dairn.steppe.GreatSteppeModule
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GreatSteppeOmenAcceptanceTest {
    @Test
    fun `an external StoryTeller resolves every published omen`() {
        (1..20).forEach { roll ->
            val omen = GreatSteppeModule.resolveOmen(roll, "ru")

            assertTrue(omen.name.isNotBlank())
            assertTrue(omen.description.isNotBlank())
        }
        assertFailsWith<IllegalArgumentException> { GreatSteppeModule.resolveOmen(0, "ru") }
        assertFailsWith<IllegalArgumentException> { GreatSteppeModule.resolveOmen(21, "ru") }
    }
}
