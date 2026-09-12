package io.github.dairn.cli

import io.github.dairn.cairn.CairnCharacterCreation
import io.github.dairn.cairn.CharacterCreationCommand
import io.github.dairn.cairn.CharacterCreationState
import io.github.dairn.core.Attribute
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
        modules.find(moduleId) ?: return error(messages.text("error.module.unknown", moduleId), messages)
        if (moduleId.value != "cairn-2e") return error(messages.text("error.character.unsupported", moduleId), messages)

        val seed = optionValue(rest, "--seed")?.toLongOrNull()
        if ("--seed" in rest && seed == null) return error(messages.text("error.seed"), messages)
        val dice = RandomDice(seed?.let(::Random) ?: Random.Default)
        val scores = List(Attribute.entries.size) { dice.roll(3, 6).total }
        val hitProtection = dice.roll(1, 6).total
        val started = CairnCharacterCreation.transition(
            CharacterCreationState.NotStarted,
            CharacterCreationCommand.Start(scores, hitProtection),
        )

        output(messages.text("character.rolls", scores.joinToString(", "), hitProtection))
        output(messages.text("character.assign.prompt"))
        val assignmentText = optionValue(rest, "--assign") ?: input()
            ?: return error(messages.text("error.assignment.missing"), messages)
        val assignment = assignmentText.split(",").mapNotNull { it.trim().toIntOrNull()?.minus(1) }
        val completed = runCatching {
            CairnCharacterCreation.transition(
                started.state,
                CharacterCreationCommand.AssignAttributes(assignment),
            )
        }.getOrElse { return error(messages.text("error.assignment.invalid"), messages) }
        val character = (completed.state as CharacterCreationState.Completed).character
        output(messages.text("character.complete"))
        output("  STR ${character.attributes.getValue(Attribute.STRENGTH)}")
        output("  DEX ${character.attributes.getValue(Attribute.DEXTERITY)}")
        output("  WIL ${character.attributes.getValue(Attribute.WILLPOWER)}")
        output("  HP  ${character.hitProtection}")
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
  --assign <1,2,3>       ${m.text("character.assign")}"""

    private fun optionValue(args: List<String>, option: String): String? {
        val index = args.indexOf(option)
        return if (index >= 0) args.getOrNull(index + 1) else null
    }

    private companion object {
        val helpFlags = setOf("-h", "--help")
    }
}
