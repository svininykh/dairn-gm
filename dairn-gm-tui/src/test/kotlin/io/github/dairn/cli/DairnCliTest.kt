package io.github.dairn.cli

import io.github.dairn.cairn.Cairn2eModule
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
            "--lang", "ru", "character", "new", "--module", "cairn-2e", "--seed", "42", "--assign", "2,1,3",
        )
        assertEquals(0, code)
        assertContains(text, "Персонаж создан")
        assertContains(text, "STR")
        assertEquals(text, execute(
            "--lang", "ru", "character", "new", "--module", "cairn-2e", "--seed", "42", "--assign", "2,1,3",
        ).second)
    }

    @Test
    fun `Great Steppe reports that creation is not implemented`() {
        val (code, text) = execute("character", "new", "--module", "great-steppe")
        assertEquals(2, code)
        assertContains(text, "not implemented")
    }

    private fun execute(vararg args: String): Pair<Int, String> {
        val lines = mutableListOf<String>()
        val code = DairnCli(registry, lines::add).run(arrayOf(*args))
        return code to lines.joinToString("\n")
    }
}
