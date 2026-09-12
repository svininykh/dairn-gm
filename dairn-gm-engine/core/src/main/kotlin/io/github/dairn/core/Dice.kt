package io.github.dairn.core

import kotlin.random.Random

data class DiceRoll(
    val dice: List<Int>,
    val sides: Int,
) {
    init {
        require(dice.isNotEmpty()) { "At least one die is required" }
        require(sides > 1) { "A die must have at least two sides" }
        require(dice.all { it in 1..sides }) { "Every result must be within the die range" }
    }

    val total: Int = dice.sum()
}

fun interface Dice {
    fun roll(count: Int, sides: Int): DiceRoll
}

class RandomDice(private val random: Random = Random.Default) : Dice {
    override fun roll(count: Int, sides: Int): DiceRoll {
        require(count > 0) { "Dice count must be positive" }
        require(sides > 1) { "A die must have at least two sides" }
        return DiceRoll(List(count) { random.nextInt(1, sides + 1) }, sides)
    }
}

