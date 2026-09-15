package org.dairn.cairn

import org.dairn.core.InteractiveCharacterCreationModule
import org.dairn.core.ModuleId
import org.dairn.core.ModuleInfo

object Cairn2eModule : InteractiveCharacterCreationModule {
    override val info = ModuleInfo(
        id = ModuleId("cairn-2e"),
        version = "0.1.0",
        name = "Cairn 2e",
    )

    override fun characterCreationProcess(languageTag: String) = CairnInteractiveCharacterCreation(languageTag)
}
