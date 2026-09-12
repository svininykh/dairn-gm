package io.github.dairn.cairn

enum class Attribute { STRENGTH, DEXTERITY, WILLPOWER }

data class CharacterBackground(
    val id: String,
    val name: String,
    val names: List<String>,
    val startingEquipment: List<String>,
    val lifepathId: String,
)

data class Lifepath(val id: String, val tables: List<LifepathTable>)

data class LifepathTable(val prompt: String, val die: String, val results: List<LifepathResult>)

data class LifepathResult(val roll: Int, val text: String)

data class LifepathExperience(val prompt: String, val roll: Int, val text: String)
