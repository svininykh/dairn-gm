package io.github.dairn.core

data class Lifepath(
    val id: String,
    val tables: List<LifepathTable>,
)

data class LifepathTable(
    val prompt: String,
    val die: String,
    val results: List<LifepathResult>,
)

data class LifepathResult(
    val roll: Int,
    val text: String,
)
