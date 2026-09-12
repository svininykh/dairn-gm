package io.github.dairn.cairn

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal object CairnCharacterData {
    private const val ROOT = "/cairn-2e/character-creation"
    private val json = Json { ignoreUnknownKeys = false }

    val backgrounds: List<CharacterBackground>
    val lifepaths: Map<String, Lifepath>
    val traits: List<CharacterTrait>

    init {
        val backgroundData = json.decodeFromString<BackgroundDocument>(resource("$ROOT/backgrounds.json"))
        val lifepathData = json.decodeFromString<LifepathDocument>(resource("$ROOT/lifepaths.json"))
        val traitData = json.decodeFromString<TraitDocument>(resource("$ROOT/traits.json"))
        backgrounds = backgroundData.backgrounds.map(BackgroundData::toDomain)
        lifepaths = lifepathData.lifepaths.map(LifepathData::toDomain).associateBy(Lifepath::id)
        traits = traitData.categories.map(TraitData::toDomain)
        validate()
    }

    private fun validate() {
        require(backgrounds.size == 20) { "Cairn 2e must define exactly 20 backgrounds" }
        require(backgrounds.map { it.id }.distinct().size == backgrounds.size) { "Background ids must be unique" }
        require(lifepaths.size == backgrounds.size) { "Every background must have one lifepath" }
        backgrounds.forEach { background ->
            require(background.names.size == 10) { "${background.id} must define exactly 10 names" }
            require(background.lifepathId in lifepaths) { "Missing lifepath ${background.lifepathId}" }
        }
        lifepaths.values.forEach { lifepath ->
            require(lifepath.tables.size == 2) { "${lifepath.id} must define exactly two lifepath tables" }
            lifepath.tables.forEach { table ->
                require(table.die == "d6") { "${lifepath.id} lifepath tables must use d6" }
                require(table.results.map { it.roll } == (1..6).toList()) { "${lifepath.id} must define results 1 through 6" }
            }
        }
        require(traits.map(CharacterTrait::id) == listOf(
            "physique", "skin", "hair", "face", "speech", "clothing", "virtue", "vice",
        )) { "Cairn 2e must define the eight character trait categories in rules order" }
        traits.forEach { trait ->
            require(trait.results.size == 10) { "${trait.id} must define exactly ten d10 results" }
        }
    }

    private fun resource(path: String): String = requireNotNull(javaClass.getResource(path)) {
        "Missing Cairn rules resource: $path"
    }.readText()
}

@Serializable
private data class BackgroundDocument(val backgrounds: List<BackgroundData>)

@Serializable
private data class BackgroundData(
    val id: String,
    val name: String,
    val names: List<String>,
    val startingEquipment: List<String>,
    val lifepathId: String,
) {
    fun toDomain() = CharacterBackground(id, name, names, startingEquipment, lifepathId)
}

@Serializable
private data class LifepathDocument(val lifepaths: List<LifepathData>)

@Serializable
private data class LifepathData(val id: String, val tables: List<LifepathTableData>) {
    fun toDomain() = Lifepath(id, tables.map(LifepathTableData::toDomain))
}

@Serializable
private data class LifepathTableData(val prompt: String, val die: String, val results: List<LifepathResultData>) {
    fun toDomain() = LifepathTable(prompt, die, results.map(LifepathResultData::toDomain))
}

@Serializable
private data class LifepathResultData(val roll: Int, val result: String) {
    fun toDomain() = LifepathResult(roll, result)
}

@Serializable
private data class TraitDocument(
    val sourceRevision: String,
    val categories: List<TraitData>,
)

@Serializable
private data class TraitData(val id: String, val name: String, val results: List<String>) {
    fun toDomain() = CharacterTrait(id, name, results)
}
