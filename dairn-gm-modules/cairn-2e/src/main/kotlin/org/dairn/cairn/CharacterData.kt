package org.dairn.cairn

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal object CairnCharacterData {
    private const val ROOT = "/cairn-2e/character-creation"
    private val json = Json { ignoreUnknownKeys = false }

    val backgrounds: List<CharacterBackground>
    val backgroundTables: Map<String, BackgroundTables>
    val traits: List<CharacterTrait>
    val bonds: List<Bond>
    val omens: List<Omen>
    private val backgroundDefinitions: List<BackgroundData>

    init {
        val backgroundData = json.decodeFromString<BackgroundDocument>(resource("$ROOT/backgrounds.json"))
        val tableData = json.decodeFromString<BackgroundTableDocument>(resource("$ROOT/background-tables.json"))
        val traitData = json.decodeFromString<TraitDocument>(resource("$ROOT/traits.json"))
        val bondData = json.decodeFromString<BondDocument>(resource("$ROOT/bonds.json"))
        val omenData = json.decodeFromString<OmenDocument>(resource("$ROOT/omens.json"))
        backgroundDefinitions = backgroundData.backgrounds
        backgrounds = backgrounds("en")
        backgroundTables = tableData.backgroundTables
            .map(BackgroundTablesData::toDomain)
            .associateBy(BackgroundTables::backgroundId)
        traits = traitData.categories.map(TraitData::toDomain)
        bonds = bondData.results.map(BondData::toDomain)
        omens = omenData.results.map(OmenData::toDomain)
        validate()
    }

    fun backgrounds(languageTag: String): List<CharacterBackground> {
        val text = CairnText(languageTag)
        return backgroundDefinitions.map { it.toDomain(text) }
    }

    private fun validate() {
        require(backgrounds.size == 20) { "Cairn 2e must define exactly 20 backgrounds" }
        require(backgrounds.map { it.id }.distinct().size == backgrounds.size) { "Background ids must be unique" }
        require(backgroundTables.size == backgrounds.size) { "Every background must have its own tables" }
        backgrounds.forEach { background ->
            require(background.names.size == 10) { "${background.id} must define exactly 10 names" }
            require(background.id in backgroundTables) { "Missing tables for ${background.id}" }
        }
        backgroundTables.values.forEach { background ->
            require(background.tables.size == 2) { "${background.backgroundId} must define exactly two background tables" }
            background.tables.forEach { table ->
                require(table.die == "d6") { "${background.backgroundId} tables must use d6" }
                require(table.results.map { it.roll } == (1..6).toList()) {
                    "${background.backgroundId} must define results 1 through 6"
                }
            }
        }
        require(traits.map(CharacterTrait::id) == listOf(
            "physique", "skin", "hair", "face", "speech", "clothing", "virtue", "vice",
        )) { "Cairn 2e must define the eight character trait categories in rules order" }
        traits.forEach { trait ->
            require(trait.resultKeys.size == 10) { "${trait.id} must define exactly ten d10 results" }
        }
        require(bonds.map(Bond::roll) == (1..20).toList()) { "Cairn 2e Bonds must define d20 results 1 through 20" }
        require(omens.map(Omen::roll) == (1..20).toList()) { "Cairn 2e Omens must define d20 results 1 through 20" }
        validateLocalization("en")
        validateLocalization("ru")
    }

    private fun validateLocalization(languageTag: String) {
        val text = CairnText(languageTag)
        val keys = buildList {
            backgroundDefinitions.forEach { background ->
                add(background.nameKey)
                addAll(background.nameKeys)
                addAll(background.startingEquipmentKeys)
            }
            backgroundTables.values.forEach { background ->
                background.tables.forEach { table ->
                    add(table.promptKey)
                    addAll(table.results.map(BackgroundTableResult::textKey))
                }
            }
            traits.forEach { trait ->
                add(trait.nameKey)
                addAll(trait.resultKeys)
            }
            addAll(bonds.map(Bond::textKey))
            addAll(omens.map(Omen::textKey))
        }
        require(keys.all(text::containsOwn)) { "Incomplete Cairn 2e $languageTag localization" }
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
    val nameKey: String,
    val nameKeys: List<String>,
    val startingEquipmentKeys: List<String>,
) {
    fun toDomain(text: CairnText) = CharacterBackground(
        id,
        text.get(nameKey),
        nameKeys.map(text::get),
        startingEquipmentKeys.map(text::get),
    )
}

@Serializable
private data class BackgroundTableDocument(val backgroundTables: List<BackgroundTablesData>)

@Serializable
private data class BackgroundTablesData(val id: String, val tables: List<BackgroundTableData>) {
    fun toDomain() = BackgroundTables(id, tables.map(BackgroundTableData::toDomain))
}

@Serializable
private data class BackgroundTableData(val promptKey: String, val die: String, val results: List<BackgroundTableResultData>) {
    fun toDomain() = BackgroundTable(promptKey, die, results.map(BackgroundTableResultData::toDomain))
}

@Serializable
private data class BackgroundTableResultData(val roll: Int, val textKey: String) {
    fun toDomain() = BackgroundTableResult(roll, textKey)
}

@Serializable
private data class TraitDocument(
    val sourceRevision: String,
    val categories: List<TraitData>,
)

@Serializable
private data class TraitData(val id: String, val nameKey: String, val resultKeys: List<String>) {
    fun toDomain() = CharacterTrait(id, nameKey, resultKeys)
}

@Serializable
private data class BondDocument(val sourceRevision: String, val die: String, val results: List<BondData>)

@Serializable
private data class BondData(val roll: Int, val textKey: String) {
    fun toDomain() = Bond(roll, textKey)
}

@Serializable
private data class OmenDocument(val sourceRevision: String, val die: String, val results: List<OmenData>)

@Serializable
private data class OmenData(val roll: Int, val textKey: String) {
    fun toDomain() = Omen(roll, textKey)
}
