package io.github.dairn.steppe

import io.github.dairn.core.DairnModule
import io.github.dairn.core.ModuleId
import io.github.dairn.core.ModuleInfo

object GreatSteppeModule : DairnModule {
    override val info = ModuleInfo(
        id = ModuleId("great-steppe"),
        version = "0.1.0",
        nameKey = "module.great-steppe.name",
    )
}

