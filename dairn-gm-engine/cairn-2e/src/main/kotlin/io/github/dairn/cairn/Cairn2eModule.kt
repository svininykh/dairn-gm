package io.github.dairn.cairn

import io.github.dairn.core.DairnModule
import io.github.dairn.core.ModuleId
import io.github.dairn.core.ModuleInfo

object Cairn2eModule : DairnModule {
    override val info = ModuleInfo(
        id = ModuleId("cairn-2e"),
        version = "0.1.0",
        nameKey = "module.cairn-2e.name",
    )
}

