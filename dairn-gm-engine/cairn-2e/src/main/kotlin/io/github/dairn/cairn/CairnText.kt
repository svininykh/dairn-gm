package io.github.dairn.cairn

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Properties

internal class CairnText(languageTag: String) {
    private val catalogs = requireNotNull(javaClass.getResource("/cairn-2e/i18n/catalogs.txt")) {
        "Missing Cairn 2e localization catalog index"
    }.readText().lineSequence().map(String::trim).filter(String::isNotEmpty).toList()

    private val fallback = loadCatalogs()
    private val localized = languageTag.lowercase()
        .takeUnless { it == "en" }
        ?.let(::loadCatalogs)

    fun get(key: String): String = localized?.getProperty(key) ?: fallback.getProperty(key)
        ?: error("Missing Cairn 2e text: $key")

    fun containsOwn(key: String): Boolean = (localized ?: fallback).containsKey(key)

    private fun loadCatalogs(languageTag: String? = null): Properties = Properties().also { merged ->
        catalogs.forEach { catalog ->
            val suffix = languageTag?.let { "_$it" }.orEmpty()
            val path = "/cairn-2e/i18n/$catalog$suffix.properties"
            val properties = loadOrNull(path)
            if (languageTag == null) requireNotNull(properties) { "Missing Cairn 2e localization resource: $path" }
            properties?.forEach { key, value ->
                require(!merged.containsKey(key)) { "Duplicate Cairn 2e localization key $key in $path" }
                merged[key] = value
            }
        }
    }

    private fun loadOrNull(path: String): Properties? = javaClass.getResourceAsStream(path)?.use { stream ->
        Properties().apply {
            InputStreamReader(stream, StandardCharsets.UTF_8).use(::load)
        }
    }
}
