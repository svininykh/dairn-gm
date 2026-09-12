package io.github.dairn.core

enum class Attribute { STRENGTH, DEXTERITY, WILLPOWER }

data class Character(
    val name: String,
    val age: Int,
    val background: String,
    val attributes: Map<Attribute, Int>,
    val hitProtection: Int,
    val goldPieces: Int,
    val inventory: List<String>,
) {
    init {
        require(name.isNotBlank()) { "Character name cannot be blank" }
        require(age in 12..50) { "Character age must be between 12 and 50" }
        require(background.isNotBlank()) { "Character background cannot be blank" }
        require(attributes.keys == Attribute.entries.toSet()) { "All character attributes are required" }
        require(attributes.values.all { it in 3..18 }) { "Attribute scores must be between 3 and 18" }
        require(hitProtection in 1..6) { "Hit Protection must be between 1 and 6" }
        require(goldPieces in 3..18) { "Starting gold must be between 3 and 18" }
    }
}
