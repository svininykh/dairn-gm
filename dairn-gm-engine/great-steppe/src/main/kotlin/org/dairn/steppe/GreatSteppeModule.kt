package org.dairn.steppe

import org.dairn.core.InteractiveCharacterCreationModule
import org.dairn.core.InteractiveGroupCreationModule
import org.dairn.core.GroupMemberInput
import org.dairn.core.ModuleId
import org.dairn.core.ModuleInfo

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
