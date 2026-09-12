package io.github.dairn.cli

import io.github.dairn.cairn.Cairn2eModule
import io.github.dairn.cairn.CairnCharacterCreation
import io.github.dairn.core.CharacterCreationModule
import io.github.dairn.core.ModuleId
import io.github.dairn.core.ModuleInfo
import io.github.dairn.core.ModuleRegistry
import io.github.dairn.steppe.GreatSteppeModule
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class DairnCliTest {
    private val registry = ModuleRegistry(listOf(Cairn2eModule, GreatSteppeModule))

    @Test
    fun `root help is localized in all supported languages`() {
        mapOf("kk" to "Қолдану", "ru" to "Использование", "en" to "Usage").forEach { (lang, word) ->
            val (code, text) = execute("--lang", lang, "--help")
            assertEquals(0, code)
            assertContains(text, word)
        }
    }

    @Test
    fun `help works at every command level`() {
        listOf(
            arrayOf("module", "--help"),
            arrayOf("module", "list", "-h"),
            arrayOf("character", "--help"),
            arrayOf("character", "new", "--help"),
        ).forEach { args -> assertEquals(0, execute(*args).first) }
    }

    @Test
    fun `module list contains both rulesets`() {
        val (code, text) = execute("module", "list")
        assertEquals(0, code)
        assertContains(text, "cairn-2e")
        assertContains(text, "great-steppe")
    }

    @Test
    fun `character new completes reproducibly for Cairn`() {
        val (code, text) = execute(
            "--lang", "ru", "character", "new", "--module", "cairn-2e", "--seed", "42", "--name", "2", "--swap", "str-wil",
        )
        assertEquals(0, code)
        assertContains(text, "Персонаж создан")
        assertContains(text, "STR")
        assertEquals(text, execute(
            "--lang", "ru", "character", "new", "--module", "cairn-2e", "--seed", "42", "--name", "2", "--swap", "str-wil",
        ).second)
    }

    @Test
    fun `Great Steppe reports that creation is not implemented`() {
        val (code, text) = execute("character", "new", "--module", "great-steppe")
        assertEquals(2, code)
        assertContains(text, "not implemented")
    }

    @Test
    fun `character creation is discovered by capability rather than module id`() {
        val customModule = object : CharacterCreationModule {
            override val info = ModuleInfo(ModuleId("custom-rules"), "test", "module.custom.name")
            override val characterCreation = CairnCharacterCreation
        }
        val lines = mutableListOf<String>()
        val code = DairnCli(ModuleRegistry(listOf(customModule)), lines::add).run(
            arrayOf("character", "new", "--module", "custom-rules", "--seed", "7", "--name", "1", "--swap", "keep"),
        )

        assertEquals(0, code)
        assertContains(lines.joinToString("\n"), "Character created")
    }

    private fun execute(vararg args: String): Pair<Int, String> {
        val lines = mutableListOf<String>()
        val code = DairnCli(registry, lines::add).run(arrayOf(*args))
        return code to lines.joinToString("\n")
    }
}
