package io.github.dairn.cli

import io.github.dairn.core.ModuleId
import io.github.dairn.core.ModuleRegistry

class DairnCli(
    private val modules: ModuleRegistry,
    private val output: (String) -> Unit = ::println,
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
        output(messages.text("character.scaffold", messages.text(module.info.nameKey)))
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
  --module <id>          ${m.text("character.module")}"""

    private companion object {
        val helpFlags = setOf("-h", "--help")
    }
}

