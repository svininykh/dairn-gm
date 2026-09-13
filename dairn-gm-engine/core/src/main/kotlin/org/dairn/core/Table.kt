package org.dairn.core

interface GameTable<T> {
    val size: Int
    fun entry(index: Int): T
}

class ListTable<T>(private val entries: List<T>) : GameTable<T> {
    init {
        require(entries.isNotEmpty()) { "A table cannot be empty" }
    }

    override val size: Int = entries.size

    override fun entry(index: Int): T {
        require(index in 1..size) { "Table index must be between 1 and $size" }
        return entries[index - 1]
    }
}

fun <T> GameTable<T>.roll(dice: Dice): T = entry(dice.roll(1, size).total)

