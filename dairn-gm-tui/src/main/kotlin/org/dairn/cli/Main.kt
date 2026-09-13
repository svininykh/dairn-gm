package org.dairn.cli

import org.dairn.cairn.Cairn2eModule
import org.dairn.core.ModuleRegistry
import org.dairn.steppe.GreatSteppeModule
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val modules = ModuleRegistry(listOf(Cairn2eModule, GreatSteppeModule))
    exitProcess(DairnCli(modules, input = ::readlnOrNull).run(args))
}
