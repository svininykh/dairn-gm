package io.github.dairn.cli

import io.github.dairn.cairn.Cairn2eModule
import io.github.dairn.core.ModuleRegistry
import io.github.dairn.steppe.GreatSteppeModule
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val modules = ModuleRegistry(listOf(Cairn2eModule, GreatSteppeModule))
    exitProcess(DairnCli(modules).run(args))
}

