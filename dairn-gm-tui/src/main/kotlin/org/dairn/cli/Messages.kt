package org.dairn.cli

import java.util.Properties

enum class Language(val code: String) {
    KK("kk"), RU("ru"), EN("en");

    companion object {
        fun fromCode(code: String): Language? = entries.find { it.code == code.lowercase() }
    }
}

class Messages(val language: Language) {
    private val defaults = load("/org/dairn/cli/messages.properties")
    private val localized = if (language == Language.EN) defaults else {
        load("/org/dairn/cli/messages_${language.code}.properties")
    }

    fun text(key: String, vararg args: Any): String {
        val template = localized.getProperty(key) ?: defaults.getProperty(key) ?: key
        return template.format(*args)
    }

    private fun load(path: String): Properties = Properties().apply {
        val stream = Messages::class.java.getResourceAsStream(path)
            ?: error("Missing message catalog: $path")
        stream.bufferedReader(Charsets.UTF_8).use(::load)
    }
}
