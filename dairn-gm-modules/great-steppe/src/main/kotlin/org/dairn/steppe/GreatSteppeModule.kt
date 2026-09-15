package org.dairn.steppe

import org.dairn.core.InteractiveCharacterCreationModule
import org.dairn.core.InteractiveInitialGroupCreationModule
import org.dairn.core.ModuleId
import org.dairn.core.ModuleInfo

object GreatSteppeModule :
    InteractiveCharacterCreationModule,
    InteractiveInitialGroupCreationModule {
    override val info = ModuleInfo(
        id = ModuleId("great-steppe"),
        version = "0.1.0",
        name = "DAIRN: Great Steppe",
    )

    override fun characterCreationProcess(languageTag: String) = GreatSteppeCharacterCreation(languageTag)

    override fun initialGroupCreationProcess(memberCount: Int, languageTag: String) =
        GreatSteppeInitialGroupCreation(memberCount, languageTag)
}
