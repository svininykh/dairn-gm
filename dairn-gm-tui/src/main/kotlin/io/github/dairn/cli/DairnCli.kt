package io.github.dairn.cli

import io.github.dairn.core.Attribute
import io.github.dairn.core.CharacterCreationCommand
import io.github.dairn.core.CharacterCreationModule
import io.github.dairn.core.CharacterCreationState
import io.github.dairn.core.ModuleId
import io.github.dairn.core.ModuleRegistry
import io.github.dairn.core.RandomDice
import kotlin.random.Random

class DairnCli(
    private val modules: ModuleRegistry,
    private val output: (String) -> Unit = ::println,
    private val input: () -> String? = { null },
) {
    fun run(arguments: Array<String>): Int {
        val parsed = parseLanguage(arguments.toList()) ?: return 2
        val messages = Messages(parsed.first)
        val args = parsed.second

        if (args.isEmpty() || args.first() in helpFlags) {
            output(rootHelp(messages))
            return 0
        }

        return when (args.first()) {
            "module" -> moduleCommand(args.drop(1), messages)
            "character" -> characterCommand(args.drop(1), messages)
            else -> error(messages.text("error.unknown", args.first()), messages)
        }
    }

    private fun moduleCommand(args: List<String>, messages: Messages): Int {
        if (args.isEmpty() || args.first() in helpFlags) {
            output(moduleHelp(messages))
            return 0
        }
        return when (args.first()) {
            "list" -> {
                if (args.drop(1).any { it !in helpFlags }) return error(messages.text("error.unknown", args.drop(1).first()), messages)
                if (args.drop(1).any { it in helpFlags }) output(moduleListHelp(messages))
                else {
                    output(messages.text("module.list.heading"))
                    modules.all().forEach { output("  ${it.info.id}\t${messages.text(it.info.nameKey)}\t${it.info.version}") }
                }
                0
            }
            else -> error(messages.text("error.unknown", args.first()), messages)
        }
    }

    private fun characterCommand(args: List<String>, messages: Messages): Int {
        if (args.isEmpty() || args.first() in helpFlags) {
            output(characterHelp(messages))
            return 0
        }
        if (args.first() != "new") return error(messages.text("error.unknown", args.first()), messages)
        val rest = args.drop(1)
        if (rest.any { it in helpFlags }) {
            output(characterNewHelp(messages))
            return 0
        }
        val moduleIndex = rest.indexOf("--module")
        if (moduleIndex < 0 || moduleIndex + 1 >= rest.size) return error(messages.text("error.module.required"), messages)
        val moduleId = runCatching { ModuleId(rest[moduleIndex + 1]) }.getOrNull()
            ?: return error(messages.text("error.module.unknown", rest[moduleIndex + 1]), messages)
        val module = modules.find(moduleId) ?: return error(messages.text("error.module.unknown", moduleId), messages)
        val creationModule = module as? CharacterCreationModule
            ?: return error(messages.text("error.character.unsupported", moduleId), messages)

        val seed = optionValue(rest, "--seed")?.toLongOrNull()
        if ("--seed" in rest && seed == null) return error(messages.text("error.seed"), messages)
        val dice = RandomDice(seed?.let(::Random) ?: Random.Default)
        val scores = List(Attribute.entries.size) { dice.roll(3, 6).total }
        val hitProtection = dice.roll(1, 6).total
        val backgroundRoll = dice.roll(1, 20).total
        val age = dice.roll(2, 20).total + 10
        val goldPieces = dice.roll(3, 6).total
        val started = creationModule.characterCreation.transition(
            CharacterCreationState.NotStarted,
            CharacterCreationCommand.Start(backgroundRoll, scores, hitProtection, age, goldPieces),
        )

        val awaitingName = started.state as CharacterCreationState.AwaitingName
        output(messages.text("character.background", awaitingName.background.name))
        awaitingName.background.names.forEachIndexed { index, name -> output("  ${index + 1}. $name") }
        output(messages.text("character.name.prompt"))
        val nameIndex = (optionValue(rest, "--name") ?: input())?.toIntOrNull()?.minus(1)
            ?: return error(messages.text("error.name.missing"), messages)
        val named = runCatching {
            creationModule.characterCreation.transition(started.state, CharacterCreationCommand.ChooseName(nameIndex))
        }.getOrElse { return error(messages.text("error.name.invalid"), messages) }

        output(messages.text("character.rolls", scores.joinToString(", "), hitProtection, age))
        output(messages.text("character.swap.prompt"))
        val swapText = optionValue(rest, "--swap") ?: input()
            ?: return error(messages.text("error.swap.missing"), messages)
        val swap = when (swapText.lowercase()) {
            "keep", "0" -> null
            "str-dex", "1" -> 0 to 1
            "str-wil", "2" -> 0 to 2
            "dex-wil", "3" -> 1 to 2
            else -> return error(messages.text("error.swap.invalid"), messages)
        }
        val completed = runCatching {
            creationModule.characterCreation.transition(
                named.state,
                CharacterCreationCommand.SwapAttributes(swap),
            )
        }.getOrElse { return error(messages.text("error.swap.invalid"), messages) }
        val character = (completed.state as CharacterCreationState.Completed).character
        output(messages.text("character.complete"))
        output("  ${messages.text("character.name")}: ${character.name}")
        output("  ${messages.text("character.age")}: ${character.age}")
        output("  ${messages.text("character.background.label")}: ${character.background}")
        output("  STR ${character.attributes.getValue(Attribute.STRENGTH)}")
        output("  DEX ${character.attributes.getValue(Attribute.DEXTERITY)}")
        output("  WIL ${character.attributes.getValue(Attribute.WILLPOWER)}")
        output("  HP  ${character.hitProtection}")
        output("  GP  ${character.goldPieces}")
        output("  ${messages.text("character.inventory")}:")
        character.inventory.forEach { output("    - $it") }
        return 0
    }

    private fun parseLanguage(args: List<String>): Pair<Language, List<String>>? {
        val index = args.indexOf("--lang")
        if (index < 0) return Language.EN to args
        val code = args.getOrNull(index + 1)
        val language = code?.let(Language::fromCode)
        if (language == null) {
            output(Messages(Language.EN).text("error.language", code ?: ""))
            return null
        }
        return language to args.filterIndexed { i, _ -> i != index && i != index + 1 }
    }

    private fun error(message: String, messages: Messages): Int {
        output(message)
        output("${messages.text("usage")}: dairn [--lang <kk|ru|en>] <command>")
        return 2
    }

    private fun rootHelp(m: Messages) = """DAIRN GM 0.1 — ${m.text("app.description")}

${m.text("usage")}: dairn [--lang <kk|ru|en>] <command>

${m.text("options")}:
  -h, --help             ${m.text("option.help")}
  --lang <kk|ru|en>      ${m.text("option.lang")}

${m.text("commands")}:
  module                 ${m.text("command.module")}
  character              ${m.text("command.character")}"""

    private fun moduleHelp(m: Messages) = """${m.text("module.description")}

${m.text("usage")}: dairn module <command>

${m.text("options")}:
  -h, --help             ${m.text("option.help")}

${m.text("commands")}:
  list                   ${m.text("module.list")}"""

    private fun moduleListHelp(m: Messages) = """${m.text("module.list")}

${m.text("usage")}: dairn module list [-h|--help]"""

    private fun characterHelp(m: Messages) = """${m.text("character.description")}

${m.text("usage")}: dairn character <command>

${m.text("options")}:
  -h, --help             ${m.text("option.help")}

${m.text("commands")}:
  new                    ${m.text("character.new")}"""

    private fun characterNewHelp(m: Messages) = """${m.text("character.new")}

${m.text("usage")}: dairn character new --module <id>

${m.text("options")}:
  -h, --help             ${m.text("option.help")}
  --module <id>          ${m.text("character.module")}
  --seed <number>        ${m.text("character.seed")}
  --name <1..10>         ${m.text("character.name.option")}
  --swap <choice>        ${m.text("character.swap.option")}"""

    private fun optionValue(args: List<String>, option: String): String? {
        val index = args.indexOf(option)
        return if (index >= 0) args.getOrNull(index + 1) else null
    }

    private companion object {
        val helpFlags = setOf("-h", "--help")
    }
}
