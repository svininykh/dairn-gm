package org.dairn.core

interface ProcessState

interface ProcessCommand

data class TransitionResult<S : ProcessState>(
    val state: S,
    val effects: List<GameEffect> = emptyList(),
    val pendingChoice: Choice.Required<*>? = null,
    val completed: Boolean = false,
)

/** Stateless transition: persistence and interaction belong to calling applications. */
fun interface GameProcess<S : ProcessState, C : ProcessCommand> {
    fun transition(state: S, command: C): TransitionResult<S>
}

