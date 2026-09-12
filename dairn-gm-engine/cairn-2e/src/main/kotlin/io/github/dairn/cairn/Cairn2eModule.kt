package io.github.dairn.cairn

import io.github.dairn.core.CharacterCreationModule
import io.github.dairn.core.InteractiveCharacterCreationModule
import io.github.dairn.core.ModuleId
import io.github.dairn.core.ModuleInfo

object Cairn2eModule : CharacterCreationModule, InteractiveCharacterCreationModule {
    override val info = ModuleInfo(
        id = ModuleId("cairn-2e"),
        version = "0.1.0",
        nameKey = "module.cairn-2e.name",
    )

    override val characterCreation = CairnCharacterCreation
    override val characterCreationProcess = CairnInteractiveCharacterCreation
}
