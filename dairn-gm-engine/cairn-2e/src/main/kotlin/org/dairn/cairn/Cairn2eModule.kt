package org.dairn.cairn

import org.dairn.core.InteractiveCharacterCreationModule
import org.dairn.core.ModuleId
import org.dairn.core.ModuleInfo

object Cairn2eModule : InteractiveCharacterCreationModule {
    override val info = ModuleInfo(
        id = ModuleId("cairn-2e"),
        version = "0.1.0",
        nameKey = "module.cairn-2e.name",
    )

    override fun characterCreationProcess(languageTag: String) = CairnInteractiveCharacterCreation(languageTag)
}
