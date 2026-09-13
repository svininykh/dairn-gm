package io.github.dairn.cairn

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Properties

internal class CairnText(languageTag: String) {
    private val fallback = load("/cairn-2e/i18n/messages.properties")
    private val localized = languageTag.lowercase()
        .takeUnless { it == "en" }
        ?.let { loadOrNull("/cairn-2e/i18n/messages_$it.properties") }

    fun get(key: String): String = localized?.getProperty(key) ?: fallback.getProperty(key)
        ?: error("Missing Cairn 2e text: $key")

    fun containsOwn(key: String): Boolean = (localized ?: fallback).containsKey(key)

    private fun load(path: String): Properties = requireNotNull(loadOrNull(path)) {
        "Missing Cairn 2e localization resource: $path"
    }

    private fun loadOrNull(path: String): Properties? = javaClass.getResourceAsStream(path)?.use { stream ->
        Properties().apply {
            InputStreamReader(stream, StandardCharsets.UTF_8).use(::load)
        }
    }
}
