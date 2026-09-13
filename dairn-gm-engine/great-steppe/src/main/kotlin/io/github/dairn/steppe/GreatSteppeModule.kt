package io.github.dairn.steppe

import io.github.dairn.core.InteractiveCharacterCreationModule
import io.github.dairn.core.InteractiveGroupCreationModule
import io.github.dairn.core.GroupMemberInput
import io.github.dairn.core.ModuleId
import io.github.dairn.core.ModuleInfo

object GreatSteppeModule : InteractiveCharacterCreationModule, InteractiveGroupCreationModule {
    override val info = ModuleInfo(
        id = ModuleId("great-steppe"),
        version = "0.1.0",
        nameKey = "module.great-steppe.name",
    )

    override fun characterCreationProcess(languageTag: String) = GreatSteppeCharacterCreation(languageTag)

    override fun groupCreationProcess(members: List<GroupMemberInput>, languageTag: String) =
        GreatSteppeGroupCreation(members, languageTag)
}
