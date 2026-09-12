package io.github.dairn.core

enum class Attribute { STRENGTH, DEXTERITY, WILLPOWER }

data class Character(
    val attributes: Map<Attribute, Int>,
    val hitProtection: Int,
) {
    init {
        require(attributes.keys == Attribute.entries.toSet()) { "All character attributes are required" }
        require(attributes.values.all { it in 3..18 }) { "Attribute scores must be between 3 and 18" }
        require(hitProtection in 1..6) { "Hit Protection must be between 1 and 6" }
    }
}

