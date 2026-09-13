package io.github.dairn.steppe

import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Properties

internal class GreatSteppeText(languageTag: String) {
    private val catalogs = requireNotNull(javaClass.getResource("/great-steppe/i18n/catalogs.txt"))
        .readText().lineSequence().map(String::trim).filter(String::isNotEmpty).toList()
    private val requested = loadCatalogs(languageTag)
    private val russianFallback = if (languageTag == "ru") requested else loadCatalogs("ru")

    fun get(key: String): String = requested.getProperty(key) ?: russianFallback.getProperty(key)
        ?: error("Missing Great Steppe text: $key")

    fun containsOwn(key: String): Boolean = requested.containsKey(key)

    private fun loadCatalogs(languageTag: String): Properties = Properties().also { merged ->
        catalogs.forEach { catalog ->
            val path = "/great-steppe/i18n/${catalog}_$languageTag.properties"
            javaClass.getResourceAsStream(path)?.use { stream ->
                val source = Properties().apply {
                    InputStreamReader(stream, StandardCharsets.UTF_8).use(::load)
                }
                source.forEach { key, value ->
                    require(!merged.containsKey(key)) { "Duplicate Great Steppe text key $key in $path" }
                    merged[key] = value
                }
            }
        }
    }
}
