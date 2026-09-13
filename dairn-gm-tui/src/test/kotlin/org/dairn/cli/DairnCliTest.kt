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
        assertContains(text, "Персонаж создан")
        assertContains(text, "СИЛ")
        assertContains(text, "Телосложение")
        assertContains(text, "Выберите Предысторию")
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
            "--text", "great-steppe.character.name=Айбек",
            "--choice", "great-steppe.character.supplies-swap=keep",
            "--text", "great-steppe.character.experience-detail=Жизнь в караване",
            "--choice", "great-steppe.character.attribute-swap=keep",
        )
        assertEquals(0, code)
        assertContains(text, "great-steppe.character")
        assertContains(text, "Айбек")
        assertContains(text, "Жизненный путь")
        assertContains(text, "Подкидыш")
        assertContains(text, "Скрытое Знамение Подкидыша")
    }

    @Test
    fun `Great Steppe group receives the common omen through the same shell`() {
        val (code, text) = execute(
            "--lang", "ru", "group", "new", "--module", "great-steppe", "--seed", "42",
            "--member", "Айбек:25",
            "--member", "Баян:31",
        )
        assertEquals(0, code)
        assertContains(text, "Группа создана")
        assertContains(text, "great-steppe.group")
        assertContains(text, "Самый молодой персонаж")
        assertContains(text, "Айбек")
        assertContains(text, "Общее Знамение Группы")
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
