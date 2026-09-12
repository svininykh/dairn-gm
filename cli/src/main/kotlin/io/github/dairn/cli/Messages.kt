package io.github.dairn.cli

enum class Language(val code: String) {
    KK("kk"), RU("ru"), EN("en");

    companion object {
        fun fromCode(code: String): Language? = entries.find { it.code == code.lowercase() }
    }
}

class Messages(private val language: Language) {
    fun text(key: String, vararg args: Any): String {
        val template = translations[language]?.get(key) ?: translations.getValue(Language.EN)[key] ?: key
        return template.format(*args)
    }

    private companion object {
        val translations = mapOf(
            Language.KK to mapOf(
                "app.description" to "DAIRN ойын жүргізушісінің модульдік құралдары",
                "usage" to "Қолдану",
                "options" to "Опциялар",
                "commands" to "Командалар",
                "option.help" to "Анықтаманы көрсету",
                "option.lang" to "Интерфейс тілі: kk, ru немесе en",
                "command.module" to "Ереже модульдерін басқару",
                "command.character" to "Кейіпкер жасау құралдары",
                "module.description" to "Қолжетімді ереже модульдері",
                "module.list" to "Барлық модульдерді көрсету",
                "module.list.heading" to "Орнатылған модульдер:",
                "character.description" to "Кейіпкер жасау командалары",
                "character.new" to "Жаңа кейіпкер жасауды бастау",
                "character.module" to "Қолданылатын ереже модулі",
                "character.scaffold" to "«%s» модулі үшін кейіпкер жасау қаңқасы дайын.",
                "module.cairn-2e.name" to "Cairn 2e",
                "module.great-steppe.name" to "DAIRN: Ұлы Дала",
                "error.unknown" to "Белгісіз команда немесе опция: %s",
                "error.language" to "Қолдау көрсетілмейтін тіл: %s",
                "error.module.required" to "--module опциясын көрсетіңіз.",
                "error.module.unknown" to "Белгісіз модуль: %s",
            ),
            Language.RU to mapOf(
                "app.description" to "Модульные инструменты ведущего DAIRN",
                "usage" to "Использование",
                "options" to "Опции",
                "commands" to "Команды",
                "option.help" to "Показать справку",
                "option.lang" to "Язык интерфейса: kk, ru или en",
                "command.module" to "Управление модулями правил",
                "command.character" to "Инструменты создания персонажа",
                "module.description" to "Доступные модули правил",
                "module.list" to "Показать все модули",
                "module.list.heading" to "Установленные модули:",
                "character.description" to "Команды создания персонажа",
                "character.new" to "Начать создание нового персонажа",
                "character.module" to "Модуль правил для создания",
                "character.scaffold" to "Каркас создания персонажа готов для модуля «%s».",
                "module.cairn-2e.name" to "Cairn 2e",
                "module.great-steppe.name" to "DAIRN: Великая Степь",
                "error.unknown" to "Неизвестная команда или опция: %s",
                "error.language" to "Неподдерживаемый язык: %s",
                "error.module.required" to "Укажите опцию --module.",
                "error.module.unknown" to "Неизвестный модуль: %s",
            ),
            Language.EN to mapOf(
                "app.description" to "Modular DAIRN game-master tools",
                "usage" to "Usage",
                "options" to "Options",
                "commands" to "Commands",
                "option.help" to "Show help",
                "option.lang" to "Interface language: kk, ru, or en",
                "command.module" to "Manage rules modules",
                "command.character" to "Character creation tools",
                "module.description" to "Available rules modules",
                "module.list" to "Show all modules",
                "module.list.heading" to "Installed modules:",
                "character.description" to "Character creation commands",
                "character.new" to "Start creating a new character",
                "character.module" to "Rules module to use",
                "character.scaffold" to "Character creation skeleton is ready for module '%s'.",
                "module.cairn-2e.name" to "Cairn 2e",
                "module.great-steppe.name" to "DAIRN: Great Steppe",
                "error.unknown" to "Unknown command or option: %s",
                "error.language" to "Unsupported language: %s",
                "error.module.required" to "Specify the --module option.",
                "error.module.unknown" to "Unknown module: %s",
            ),
        )
    }
}

