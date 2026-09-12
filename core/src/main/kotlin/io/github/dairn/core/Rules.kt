package io.github.dairn.core

/** A result produced by a rule. Modules define their own concrete effects. */
interface GameEffect

/** A pure rules operation from a module-defined context to zero or more effects. */
fun interface Rule<C> {
    fun evaluate(context: C): List<GameEffect>
}

