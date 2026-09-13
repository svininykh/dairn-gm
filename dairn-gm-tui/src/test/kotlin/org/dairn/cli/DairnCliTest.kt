package org.dairn.cli

import org.dairn.cairn.Cairn2eModule
import org.dairn.cairn.CairnInteractiveCharacterCreation
import org.dairn.core.InteractiveCharacterCreationModule
import org.dairn.core.ModuleId
import org.dairn.core.ModuleInfo
import org.dairn.core.ModuleRegistry
import org.dairn.steppe.GreatSteppeModule
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class DairnCliTest {
    private val registry = ModuleRegistry(listOf(Cairn2eModule, GreatSteppeModule))

    @Test
    fun `root help is localized in all supported languages`() {
        Language.entries.forEach { language ->
            val lang = language.code
            val word = Messages(language).text("usage")
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
            arrayOf("group", "--help"),
            arrayOf("group", "new", "--help"),
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
            "--lang", "ru", "character", "new", "--module", "cairn-2e", "--seed", "42",
            "--choice", "cairn-2e.character.background=roll",
            "--choice", "cairn-2e.character.name=1",
            "--choice", "cairn-2e.character.attribute-swap=str-wil",
        )
        assertEquals(0, code)
        assertContains(text, Messages(Language.RU).text("character.complete"))
        assertContains(text, "cairn-2e.character")
        assertEquals(text, execute(
            "--lang", "ru", "character", "new", "--module", "cairn-2e", "--seed", "42",
            "--choice", "cairn-2e.character.background=roll",
            "--choice", "cairn-2e.character.name=1",
            "--choice", "cairn-2e.character.attribute-swap=str-wil",
        ).second)
    }

    @Test
    fun `Great Steppe uses its own creation process through the same shell`() {
        val (code, text) = execute(
            "--lang", "ru", "character", "new", "--module", "great-steppe", "--seed", "42",
            "--choice", "great-steppe.character.life-path=18",
            "--text", "great-steppe.character.name=Aibek",
            "--choice", "great-steppe.character.supplies-swap=keep",
            "--text", "great-steppe.character.experience-detail=Caravan life",
            "--choice", "great-steppe.character.attribute-swap=keep",
        )
        assertEquals(0, code)
        assertContains(text, "great-steppe.character")
        assertContains(text, "Aibek")
    }

    @Test
    fun `Great Steppe group receives the common omen through the same shell`() {
        val (code, text) = execute(
            "--lang", "ru", "group", "new", "--module", "great-steppe", "--seed", "42",
            "--member", "Aibek:25",
            "--member", "Bayan:31",
        )
        assertEquals(0, code)
        assertContains(text, Messages(Language.RU).text("group.complete"))
        assertContains(text, "great-steppe.group")
        assertContains(text, "Aibek")
    }

    @Test
    fun `character creation is discovered by capability rather than module id`() {
        val customModule = object : InteractiveCharacterCreationModule {
            override val info = ModuleInfo(ModuleId("custom-rules"), "test", "module.custom.name")
            override fun characterCreationProcess(languageTag: String) = CairnInteractiveCharacterCreation(languageTag)
        }
        val lines = mutableListOf<String>()
        val code = DairnCli(ModuleRegistry(listOf(customModule)), lines::add).run(
            arrayOf(
                "character", "new", "--module", "custom-rules", "--seed", "7",
                "--choice", "cairn-2e.character.background=roll",
                "--choice", "cairn-2e.character.name=0",
                "--choice", "cairn-2e.character.attribute-swap=keep",
            ),
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
