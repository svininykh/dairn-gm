package org.dairn.steppe

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.Properties

internal object GreatSteppeCharacterData {
    const val SOURCE_REVISION = "3fabe55fa366c3706a5c54614b1682bd2f7cb5d6"
    private const val ROOT = "/great-steppe/character-creation"
    private val json = Json { ignoreUnknownKeys = false }

    val lifePaths = decode<LifePathDocument>("life-paths.json").results
    val inventory = decode<InventoryDocument>("inventory.json").results
    val traits = decode<TraitDocument>("traits.json").categories
    val bonds = decode<TextTableDocument>("bonds.json").results
    val omens = decode<OmenDocument>("omens.json").results

    init {
        require(lifePaths.map(LifePathDefinition::roll) == (1..20).toList())
        require(inventory.map(StartingInventoryResult::roll) == (1..6).toList())
        require(traits.size == 8 && traits.all { it.resultKeys.size == 10 })
        require(bonds.map(TextTableResult::roll) == (1..20).toList())
        require(omens.map(OmenDefinition::roll) == (1..20).toList())
        validateRussianKeys()
    }

    private fun validateRussianKeys() {
        val catalogs = requireNotNull(javaClass.getResource("/great-steppe/i18n/catalogs.txt"))
            .readText().lineSequence().map(String::trim).filter(String::isNotEmpty)
        val properties = Properties()
        catalogs.forEach { catalog ->
            val path = "/great-steppe/i18n/${catalog}_ru.properties"
            requireNotNull(javaClass.getResourceAsStream(path)) { "Missing Great Steppe resource: $path" }.use { stream ->
                val source = Properties().apply {
                    InputStreamReader(stream, StandardCharsets.UTF_8).use(::load)
                }
                source.forEach { key, value ->
                    require(!properties.containsKey(key)) { "Duplicate Great Steppe text key: $key" }
                    properties[key] = value
                }
            }
        }
        require(allTextKeys().all(properties::containsKey)) { "Incomplete Great Steppe Russian localization" }
    }

    private fun allTextKeys(): List<String> = buildList {
        lifePaths.forEach { add(it.nameKey); add(it.experienceKey); add(it.uniqueElementKey) }
        inventory.forEach {
            add(it.weaponKey); add(it.travelGearKey); add(it.toolKey)
            add(it.waterKey); add(it.foodKey); add(it.fireKey)
        }
        traits.forEach { add(it.nameKey); addAll(it.resultKeys) }
        addAll(bonds.map(TextTableResult::textKey))
        omens.forEach { add(it.nameKey); add(it.descriptionKey) }
    }

    private inline fun <reified T> decode(file: String): T {
        val content = requireNotNull(javaClass.getResource("$ROOT/$file")) { "Missing Great Steppe data: $file" }.readText()
        val sourceRevision = json.parseToJsonElement(content).jsonObject["sourceRevision"]?.toString()?.trim('"')
        require(sourceRevision == SOURCE_REVISION) { "Unexpected Great Steppe source revision in $file" }
        return json.decodeFromString(content)
    }
}

@Serializable
internal data class LifePathDefinition(val roll: Int, val nameKey: String, val experienceKey: String, val uniqueElementKey: String)

@Serializable
private data class LifePathDocument(val sourceRevision: String, val sourceLanguage: String, val die: String, val results: List<LifePathDefinition>)

@Serializable
internal data class StartingInventoryResult(
    val roll: Int,
    val weaponKey: String,
    val travelGearKey: String,
    val toolKey: String,
    val waterKey: String,
    val foodKey: String,
    val fireKey: String,
)

@Serializable
private data class InventoryDocument(val sourceRevision: String, val sourceLanguage: String, val die: String, val results: List<StartingInventoryResult>)

@Serializable
internal data class TraitDefinition(val id: String, val nameKey: String, val resultKeys: List<String>)

@Serializable
private data class TraitDocument(val sourceRevision: String, val sourceLanguage: String, val die: String, val categories: List<TraitDefinition>)

@Serializable
internal data class TextTableResult(val roll: Int, val textKey: String)

@Serializable
private data class TextTableDocument(val sourceRevision: String, val sourceLanguage: String, val die: String, val results: List<TextTableResult>)

@Serializable
internal data class OmenDefinition(val roll: Int, val nameKey: String, val descriptionKey: String)

@Serializable
private data class OmenDocument(val sourceRevision: String, val sourceLanguage: String, val die: String, val results: List<OmenDefinition>)
