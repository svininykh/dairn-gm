package org.dairn.steppe

import org.dairn.core.InteractiveCharacterCreationModule
import org.dairn.core.InteractiveInitialGroupCreationModule
import org.dairn.core.ModuleId
import org.dairn.core.ModuleInfo

/** Resolves a published Great Steppe Omen table result without an interactive process. */
interface GreatSteppeOmenResolver {
    fun resolveOmen(roll: Int, languageTag: String = "ru"): GreatSteppeOmen
}

object GreatSteppeModule :
    InteractiveCharacterCreationModule,
    InteractiveInitialGroupCreationModule,
    GreatSteppeOmenResolver {
    override val info = ModuleInfo(
        id = ModuleId("great-steppe"),
        version = "0.1.0",
        name = "DAIRN: Great Steppe",
    )

    override fun characterCreationProcess(languageTag: String) = GreatSteppeCharacterCreation(languageTag)

    override fun initialGroupCreationProcess(memberCount: Int, languageTag: String) =
        GreatSteppeInitialGroupCreation(memberCount, languageTag)

    override fun resolveOmen(roll: Int, languageTag: String): GreatSteppeOmen {
        require(roll in 1..20) { "Omen roll must be between 1 and 20" }
        val definition = GreatSteppeCharacterData.omens[roll - 1]
        val text = GreatSteppeText(languageTag)
        return GreatSteppeOmen(roll, text.get(definition.nameKey), text.get(definition.descriptionKey))
    }
}
