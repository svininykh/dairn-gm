package io.github.dairn.core

data class CharacterBackground(
    val id: String,
    val name: String,
    val names: List<String>,
    val startingEquipment: List<String>,
) {
    init {
        require(id.isNotBlank() && name.isNotBlank())
        require(names.isNotEmpty() && names.none(String::isBlank))
        require(startingEquipment.isNotEmpty() && startingEquipment.none(String::isBlank))
    }
}
