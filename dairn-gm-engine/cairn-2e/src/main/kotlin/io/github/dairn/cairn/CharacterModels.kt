package io.github.dairn.cairn

enum class Attribute { STRENGTH, DEXTERITY, WILLPOWER }

data class CharacterBackground(
    val id: String,
    val name: String,
    val names: List<String>,
    val startingEquipment: List<String>,
)

data class BackgroundTables(val backgroundId: String, val tables: List<BackgroundTable>)

data class BackgroundTable(val promptKey: String, val die: String, val results: List<BackgroundTableResult>)

data class BackgroundTableResult(val roll: Int, val textKey: String)

data class ResolvedBackgroundTable(val prompt: String, val roll: Int, val text: String)

data class CharacterTrait(val id: String, val nameKey: String, val resultKeys: List<String>)

data class RolledCharacterTrait(val id: String, val name: String, val roll: Int, val result: String)

data class Bond(val roll: Int, val textKey: String)

data class ResolvedBond(val roll: Int, val text: String)

data class Omen(val roll: Int, val textKey: String)
