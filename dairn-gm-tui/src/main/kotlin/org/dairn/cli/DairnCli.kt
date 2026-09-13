package org.dairn.cli

import org.dairn.core.*
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
            "group" -> groupCommand(args.drop(1), messages)
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
        val creationModule = module as? InteractiveCharacterCreationModule
            ?: return error(messages.text("error.character.unsupported", moduleId), messages)

        val seed = optionValue(rest, "--seed")?.toLongOrNull()
        if ("--seed" in rest && seed == null) return error(messages.text("error.seed"), messages)
        val dice = RandomDice(seed?.let(::Random) ?: Random.Default)
        val choices = optionValues(rest, "--choice").mapNotNull(::parseAssignment).toMap()
        val texts = optionValues(rest, "--text").mapNotNull(::parseAssignment).toMap()
        return runInteractive(creationModule.characterCreationProcess(messages.language.code), dice, choices, texts, messages, "character.complete")
    }

    private fun groupCommand(args: List<String>, messages: Messages): Int {
        if (args.isEmpty() || args.first() in helpFlags) {
            output(groupHelp(messages))
            return 0
        }
        if (args.first() != "new") return error(messages.text("error.unknown", args.first()), messages)
        val rest = args.drop(1)
        if (rest.any { it in helpFlags }) {
            output(groupNewHelp(messages))
            return 0
        }
        val moduleValue = optionValue(rest, "--module")
            ?: return error(messages.text("error.module.required"), messages)
        val moduleId = runCatching { ModuleId(moduleValue) }.getOrNull()
            ?: return error(messages.text("error.module.unknown", moduleValue), messages)
        val module = modules.find(moduleId) ?: return error(messages.text("error.module.unknown", moduleId), messages)
        val creationModule = module as? InteractiveGroupCreationModule
            ?: return error(messages.text("error.group.unsupported", moduleId), messages)
        val members = optionValues(rest, "--member").mapIndexed { index, value ->
            parseMember(value, index + 1) ?: return error(messages.text("error.member", value), messages)
        }
        if (members.isEmpty()) return error(messages.text("error.member.required"), messages)
        val seed = optionValue(rest, "--seed")?.toLongOrNull()
        if ("--seed" in rest && seed == null) return error(messages.text("error.seed"), messages)
        val choices = optionValues(rest, "--choice").mapNotNull(::parseAssignment).toMap()
        return runInteractive(
            creationModule.groupCreationProcess(members, messages.language.code),
            RandomDice(seed?.let(::Random) ?: Random.Default),
            choices,
            emptyMap(),
            messages,
            "group.complete",
        )
    }

    private fun runInteractive(
        process: InteractiveProcess,
        dice: Dice,
        suppliedChoices: Map<String, String>,
        suppliedTexts: Map<String, String>,
        messages: Messages,
        completionKey: String,
    ): Int {
        var step: InteractiveStep = process.start()
        while (step is InteractiveStep.Waiting) {
            val waiting = step
            val response = when (val request = waiting.request) {
                is ProcessRequest.Roll -> {
                    val totals = request.rolls.associate { spec ->
                        spec.id to (dice.roll(spec.dice.count, spec.dice.sides).total + spec.dice.modifier)
                    }
                    output("${request.prompt}:")
                    totals.forEach { (id, total) -> output("  $id = $total") }
                    ProcessResponse.Rolled(request.id, totals)
                }
                is ProcessRequest.Choose -> {
                    output(request.prompt)
                    request.options.forEachIndexed { index, option -> output("  ${index + 1}. ${option.label} [${option.id}]") }
                    val entered = suppliedChoices[request.id.value] ?: input()
                        ?: return error(messages.text("error.response.missing", request.id.value), messages)
                    val selected = request.options.getOrNull(entered.toIntOrNull()?.minus(1) ?: -1)?.id ?: entered
                    if (request.options.none { it.id == selected }) {
                        return error(messages.text("error.response.invalid", request.id.value), messages)
                    }
                    ProcessResponse.Selected(request.id, listOf(selected))
                }
                is ProcessRequest.EnterText -> {
                    output(request.prompt)
                    val value = suppliedTexts[request.id.value] ?: input()
                        ?: return error(messages.text("error.response.missing", request.id.value), messages)
                    if (!request.allowBlank && value.isBlank()) {
                        return error(messages.text("error.response.invalid", request.id.value), messages)
                    }
                    ProcessResponse.TextEntered(request.id, value)
                }
            }
            step = runCatching { process.advance(waiting.state, response) }
                .getOrElse { return error(messages.text("error.process", it.message ?: ""), messages) }
        }
        val artifact = (step as InteractiveStep.Completed).artifact
        output(messages.text(completionKey))
        output("  type: ${artifact.type}")
        artifact.fields.forEach { field ->
            output("  ${field.label}:")
            field.values.forEach { output("    $it") }
        }
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
  character              ${m.text("command.character")}
  group                  ${m.text("command.group")}"""

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
  --choice <id=value>    ${m.text("character.choice.option")}
  --text <id=value>      Supply a text response non-interactively"""

    private fun groupHelp(m: Messages) = """${m.text("group.description")}

${m.text("usage")}: dairn group <command>

${m.text("options")}:
  -h, --help             ${m.text("option.help")}

${m.text("commands")}:
  new                    ${m.text("group.new")}"""

    private fun groupNewHelp(m: Messages) = """${m.text("group.new")}

${m.text("usage")}: dairn group new --module <id> --member <name:age> [--member <name:age> ...]

${m.text("options")}:
  -h, --help             ${m.text("option.help")}
  --module <id>          ${m.text("character.module")}
  --member <name:age>    ${m.text("group.member")}
  --seed <number>        ${m.text("character.seed")}
  --choice <id=value>    ${m.text("character.choice.option")}"""

    private fun optionValue(args: List<String>, option: String): String? {
        val index = args.indexOf(option)
        return if (index >= 0) args.getOrNull(index + 1) else null
    }

    private fun optionValues(args: List<String>, option: String): List<String> = args.mapIndexedNotNull { index, value ->
        if (value == option) args.getOrNull(index + 1) else null
    }

    private fun parseAssignment(value: String): Pair<String, String>? {
        val separator = value.indexOf('=')
        return if (separator > 0 && separator < value.lastIndex) {
            value.substring(0, separator) to value.substring(separator + 1)
        } else null
    }

    private fun parseMember(value: String, number: Int): GroupMemberInput? {
        val separator = value.lastIndexOf(':')
        if (separator <= 0 || separator == value.lastIndex) return null
        val name = value.substring(0, separator).trim()
        val age = value.substring(separator + 1).toIntOrNull() ?: return null
        return runCatching { GroupMemberInput("member-$number", name, age) }.getOrNull()
    }

    private companion object {
        val helpFlags = setOf("-h", "--help")
    }
}
