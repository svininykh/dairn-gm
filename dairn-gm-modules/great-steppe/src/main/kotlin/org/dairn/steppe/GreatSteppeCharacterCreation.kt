package org.dairn.steppe

import org.dairn.core.*

data class GreatSteppeLifePath(
    val roll: Int,
    val name: String,
    val experience: String,
    val detail: String,
    val uniqueElement: String,
)

enum class GreatSteppeInventoryCategory(val id: String) {
    WATER("water"), FOOD("food"), FIRE("fire"), WEAPON("weapon"),
    TRAVEL_GEAR("travel-gear"), TOOL("tool")
}

enum class GreatSteppeSupplyUnit { DAYS, NIGHTS }

data class GreatSteppeSupply(val amount: Int, val unit: GreatSteppeSupplyUnit) {
    init {
        require(amount >= 0) { "Supply amount cannot be negative" }
    }
}

data class GreatSteppeInventoryItem(
    val id: String,
    val category: GreatSteppeInventoryCategory,
    val name: String,
    val slots: Int,
    val supply: GreatSteppeSupply? = null,
) {
    init {
        require(id.isNotBlank())
        require(name.isNotBlank())
        require(slots in 0..2)
    }

    val bulky: Boolean get() = slots == 2
    fun asCoreEntry() = InventoryEntry(id, slots)
}

data class GreatSteppeInventory(val items: List<GreatSteppeInventoryItem>) {
    init {
        require(items.map(GreatSteppeInventoryItem::category).toSet() == GreatSteppeInventoryCategory.entries.toSet())
        require(items.map(GreatSteppeInventoryItem::id).distinct().size == items.size)
    }

    val load = InventoryLoad(GREAT_STEPPE_INVENTORY_CAPACITY, items.map(GreatSteppeInventoryItem::asCoreEntry))

    operator fun get(category: GreatSteppeInventoryCategory): GreatSteppeInventoryItem =
        items.single { it.category == category }

    fun asList(): List<String> = items.map(GreatSteppeInventoryItem::name)
}

data class GreatSteppeTrait(val id: String, val name: String, val roll: Int, val result: String)
data class GreatSteppeBond(val roll: Int, val text: String)
data class GreatSteppeOmen(val roll: Int, val name: String, val description: String)

data class GreatSteppeCharacter(
    val name: String,
    val lifePath: GreatSteppeLifePath,
    val inventory: GreatSteppeInventory,
    val attributes: List<Int>,
    val hitProtection: Int,
    val traits: List<GreatSteppeTrait>,
    val bond: GreatSteppeBond,
    val age: Int,
    val secretOmen: GreatSteppeOmen?,
    private val labels: Map<String, String>,
) : ProcessArtifact {
    override val type = "great-steppe.character"
    override val fields: List<ArtifactField>
        get() = buildList {
            add(ArtifactField(labels.getValue("name"), name))
            add(ArtifactField(labels.getValue("life-path"), lifePath.name))
            add(ArtifactField(labels.getValue("experience"), listOf(lifePath.experience, lifePath.detail).filter(String::isNotBlank)))
            add(ArtifactField(labels.getValue("unique-element"), lifePath.uniqueElement))
            add(ArtifactField(labels.getValue("inventory"), inventory.asList()))
            add(ArtifactField(labels.getValue("inventory-load"), "${inventory.load.occupiedSlots}/${inventory.load.capacity}"))
            add(ArtifactField(labels.getValue("str"), attributes[0]))
            add(ArtifactField(labels.getValue("dex"), attributes[1]))
            add(ArtifactField(labels.getValue("wil"), attributes[2]))
            add(ArtifactField(labels.getValue("hp"), hitProtection))
            traits.forEach { add(ArtifactField(it.name, it.result)) }
            add(ArtifactField(labels.getValue("bond"), bond.text))
            add(ArtifactField(labels.getValue("age"), age))
            secretOmen?.let {
                add(ArtifactField(labels.getValue("secret-omen"), listOf(it.name, it.description)))
            }
        }
}

private data class Draft(
    val lifePath: LifePathDefinition? = null,
    val name: String? = null,
    val inventoryRolls: Map<String, Int> = emptyMap(),
    val experienceDetail: String = "",
    val attributes: List<Int> = emptyList(),
    val hitProtection: Int? = null,
    val traits: List<GreatSteppeTrait> = emptyList(),
    val bond: GreatSteppeBond? = null,
    val age: Int? = null,
)

private data class State(
    val stage: Stage,
    val draft: Draft,
    val request: ProcessRequest,
) : ProcessState

private enum class Stage {
    LIFE_PATH, LIFE_PATH_ROLL, NAME, INVENTORY, SUPPLIES_SWAP, EXPERIENCE,
    ABILITIES, ATTRIBUTES_SWAP, TRAITS_BOND, AGE, FOUNDLING_OMEN,
}

class GreatSteppeCharacterCreation(languageTag: String = "ru") : InteractiveProcess {
    override val id = ProcessId("great-steppe.character-creation")
    private val text = GreatSteppeText(languageTag)

    override fun start(): InteractiveStep.Waiting {
        val request = ProcessRequest.Choose(
            RequestId("great-steppe.character.life-path"),
            text.get("process.life-path.prompt"),
            listOf(ChoiceOption("roll", text.get("process.life-path.roll"))) +
                GreatSteppeCharacterData.lifePaths.map { ChoiceOption(it.roll.toString(), text.get(it.nameKey)) },
        )
        return waiting(Stage.LIFE_PATH, Draft(), request)
    }

    override fun advance(state: ProcessState, response: ProcessResponse): InteractiveStep {
        require(state is State) { "State does not belong to ${id.value}" }
        state.request.requireMatching(response)
        return when (state.stage) {
            Stage.LIFE_PATH -> acceptLifePath(state, response)
            Stage.LIFE_PATH_ROLL -> acceptLifePathRoll(state, response)
            Stage.NAME -> acceptName(state, response)
            Stage.INVENTORY -> acceptInventory(state, response)
            Stage.SUPPLIES_SWAP -> acceptSuppliesSwap(state, response)
            Stage.EXPERIENCE -> acceptExperience(state, response)
            Stage.ABILITIES -> acceptAbilities(state, response)
            Stage.ATTRIBUTES_SWAP -> acceptAttributesSwap(state, response)
            Stage.TRAITS_BOND -> acceptTraitsAndBond(state, response)
            Stage.AGE -> acceptAge(state, response)
            Stage.FOUNDLING_OMEN -> acceptFoundlingOmen(state, response)
        }
    }

    private fun acceptLifePath(state: State, response: ProcessResponse): InteractiveStep.Waiting {
        val selected = selected(state.request as ProcessRequest.Choose, response)
        if (selected == "roll") {
            val request = ProcessRequest.Roll(
                RequestId("great-steppe.character.life-path-roll"),
                text.get("process.life-path-roll.prompt"),
                listOf(RollSpec("life-path", DiceExpression(1, 20))),
            )
            return waiting(Stage.LIFE_PATH_ROLL, state.draft, request)
        }
        return requestName(state.draft.copy(lifePath = lifePath(selected.toInt())))
    }

    private fun acceptLifePathRoll(state: State, response: ProcessResponse): InteractiveStep.Waiting =
        requestName(state.draft.copy(lifePath = lifePath(rolled(state.request as ProcessRequest.Roll, response).getValue("life-path"))))

    private fun requestName(draft: Draft): InteractiveStep.Waiting {
        val request = ProcessRequest.EnterText(
            RequestId("great-steppe.character.name"),
            text.get("process.name.prompt"),
        )
        return waiting(Stage.NAME, draft, request)
    }

    private fun acceptName(state: State, response: ProcessResponse): InteractiveStep.Waiting {
        val name = entered(state.request as ProcessRequest.EnterText, response)
        val request = ProcessRequest.Roll(
            RequestId("great-steppe.character.inventory"),
            text.get("process.inventory.prompt"),
            listOf("water", "food", "fire", "weapon", "travel-gear", "tool")
                .map { RollSpec(it, DiceExpression(1, 6)) },
        )
        return waiting(Stage.INVENTORY, state.draft.copy(name = name), request)
    }

    private fun acceptInventory(state: State, response: ProcessResponse): InteractiveStep.Waiting {
        val totals = rolled(state.request as ProcessRequest.Roll, response)
        val request = ProcessRequest.Choose(
            RequestId("great-steppe.character.supplies-swap"),
            text.get("process.supplies-swap.prompt"),
            listOf(
                ChoiceOption("keep", text.get("process.supplies-swap.keep")),
                ChoiceOption("water-food", text.get("process.supplies-swap.water-food")),
                ChoiceOption("water-fire", text.get("process.supplies-swap.water-fire")),
                ChoiceOption("food-fire", text.get("process.supplies-swap.food-fire")),
            ),
        )
        return waiting(Stage.SUPPLIES_SWAP, state.draft.copy(inventoryRolls = totals), request)
    }

    private fun acceptSuppliesSwap(state: State, response: ProcessResponse): InteractiveStep.Waiting {
        val choice = selected(state.request as ProcessRequest.Choose, response)
        val rolls = state.draft.inventoryRolls.toMutableMap()
        when (choice) {
            "water-food" -> rolls.swap("water", "food")
            "water-fire" -> rolls.swap("water", "fire")
            "food-fire" -> rolls.swap("food", "fire")
        }
        val request = ProcessRequest.EnterText(
            RequestId("great-steppe.character.experience-detail"),
            text.get("process.experience-detail.prompt"),
            allowBlank = true,
        )
        return waiting(Stage.EXPERIENCE, state.draft.copy(inventoryRolls = rolls), request)
    }

    private fun acceptExperience(state: State, response: ProcessResponse): InteractiveStep.Waiting {
        val detail = entered(state.request as ProcessRequest.EnterText, response)
        val request = ProcessRequest.Roll(
            RequestId("great-steppe.character.abilities"),
            text.get("process.abilities.prompt"),
            listOf(
                RollSpec("str", DiceExpression(3, 6)),
                RollSpec("dex", DiceExpression(3, 6)),
                RollSpec("wil", DiceExpression(3, 6)),
                RollSpec("hp", DiceExpression(1, 6)),
            ),
        )
        return waiting(Stage.ABILITIES, state.draft.copy(experienceDetail = detail), request)
    }

    private fun acceptAbilities(state: State, response: ProcessResponse): InteractiveStep.Waiting {
        val totals = rolled(state.request as ProcessRequest.Roll, response)
        val request = ProcessRequest.Choose(
            RequestId("great-steppe.character.attribute-swap"),
            text.get("process.attributes-swap.prompt"),
            listOf(
                ChoiceOption("keep", text.get("process.attributes-swap.keep")),
                ChoiceOption("str-dex", text.get("process.attributes-swap.str-dex")),
                ChoiceOption("str-wil", text.get("process.attributes-swap.str-wil")),
                ChoiceOption("dex-wil", text.get("process.attributes-swap.dex-wil")),
            ),
        )
        return waiting(
            Stage.ATTRIBUTES_SWAP,
            state.draft.copy(
                attributes = listOf("str", "dex", "wil").map(totals::getValue),
                hitProtection = totals.getValue("hp"),
            ),
            request,
        )
    }

    private fun acceptAttributesSwap(state: State, response: ProcessResponse): InteractiveStep.Waiting {
        val choice = selected(state.request as ProcessRequest.Choose, response)
        val scores = state.draft.attributes.toMutableList()
        when (choice) {
            "str-dex" -> scores.swap(0, 1)
            "str-wil" -> scores.swap(0, 2)
            "dex-wil" -> scores.swap(1, 2)
        }
        val request = ProcessRequest.Roll(
            RequestId("great-steppe.character.traits-and-bond"),
            text.get("process.traits-bond.prompt"),
            GreatSteppeCharacterData.traits.map { RollSpec("trait-${it.id}", DiceExpression(1, 10)) } +
                RollSpec("bond", DiceExpression(1, 20)),
        )
        return waiting(Stage.TRAITS_BOND, state.draft.copy(attributes = scores), request)
    }

    private fun acceptTraitsAndBond(state: State, response: ProcessResponse): InteractiveStep.Waiting {
        val totals = rolled(state.request as ProcessRequest.Roll, response)
        val traits = GreatSteppeCharacterData.traits.map { trait ->
            val roll = totals.getValue("trait-${trait.id}")
            GreatSteppeTrait(trait.id, text.get(trait.nameKey), roll, text.get(trait.resultKeys[roll - 1]))
        }
        val bondDefinition = GreatSteppeCharacterData.bonds[totals.getValue("bond") - 1]
        val request = ProcessRequest.Roll(
            RequestId("great-steppe.character.age"),
            text.get("process.age.prompt"),
            listOf(RollSpec("age", DiceExpression(2, 20, 10))),
        )
        return waiting(
            Stage.AGE,
            state.draft.copy(
                traits = traits,
                bond = GreatSteppeBond(bondDefinition.roll, text.get(bondDefinition.textKey)),
            ),
            request,
        )
    }

    private fun acceptAge(state: State, response: ProcessResponse): InteractiveStep {
        val age = rolled(state.request as ProcessRequest.Roll, response).getValue("age")
        val draft = state.draft.copy(age = age)
        if (draft.lifePath?.roll == FOUNDLING_ROLL) {
            val request = ProcessRequest.Roll(
                RequestId("great-steppe.character.foundling-omen"),
                text.get("process.foundling-omen.prompt"),
                listOf(RollSpec("omen", DiceExpression(1, 20))),
            )
            return waiting(Stage.FOUNDLING_OMEN, draft, request)
        }
        return complete(draft, null)
    }

    private fun acceptFoundlingOmen(state: State, response: ProcessResponse): InteractiveStep.Completed {
        val roll = rolled(state.request as ProcessRequest.Roll, response).getValue("omen")
        val definition = GreatSteppeCharacterData.omens[roll - 1]
        return complete(
            state.draft,
            GreatSteppeOmen(roll, text.get(definition.nameKey), text.get(definition.descriptionKey)),
        )
    }

    private fun complete(draft: Draft, secretOmen: GreatSteppeOmen?): InteractiveStep.Completed {
        val definition = requireNotNull(draft.lifePath)
        val lifePath = GreatSteppeLifePath(
            definition.roll,
            text.get(definition.nameKey),
            text.get(definition.experienceKey),
            draft.experienceDetail,
            text.get(definition.uniqueElementKey),
        )
        fun inventoryItem(category: GreatSteppeInventoryCategory): GreatSteppeInventoryItem {
            val categoryId = category.id
            val roll = draft.inventoryRolls.getValue(categoryId)
            val row = GreatSteppeCharacterData.inventory[roll - 1]
            val (key, slots) = when (category) {
                GreatSteppeInventoryCategory.WATER -> row.waterKey to row.waterSlots
                GreatSteppeInventoryCategory.FOOD -> row.foodKey to row.foodSlots
                GreatSteppeInventoryCategory.FIRE -> row.fireKey to row.fireSlots
                GreatSteppeInventoryCategory.WEAPON -> row.weaponKey to row.weaponSlots
                GreatSteppeInventoryCategory.TRAVEL_GEAR -> row.travelGearKey to row.travelGearSlots
                GreatSteppeInventoryCategory.TOOL -> row.toolKey to row.toolSlots
            }
            val supply = when (category) {
                GreatSteppeInventoryCategory.WATER -> GreatSteppeSupply(row.waterDays, GreatSteppeSupplyUnit.DAYS)
                GreatSteppeInventoryCategory.FOOD -> GreatSteppeSupply(row.foodDays, GreatSteppeSupplyUnit.DAYS)
                GreatSteppeInventoryCategory.FIRE -> GreatSteppeSupply(row.fireNights, GreatSteppeSupplyUnit.NIGHTS)
                else -> null
            }
            return GreatSteppeInventoryItem(
                "great-steppe.starting.$categoryId",
                category,
                text.get(key),
                slots,
                supply,
            )
        }
        val inventory = GreatSteppeInventory(GreatSteppeInventoryCategory.entries.map(::inventoryItem))
        val labelIds = listOf(
            "name", "life-path", "experience", "unique-element", "inventory", "inventory-load",
            "str", "dex", "wil", "hp", "bond", "age", "secret-omen",
        )
        return InteractiveStep.Completed(
            GreatSteppeCharacter(
                requireNotNull(draft.name),
                lifePath,
                inventory,
                draft.attributes,
                requireNotNull(draft.hitProtection),
                draft.traits,
                requireNotNull(draft.bond),
                requireNotNull(draft.age),
                secretOmen,
                labelIds.associateWith { text.get("field.$it") },
            ),
        )
    }

    private fun lifePath(roll: Int): LifePathDefinition = GreatSteppeCharacterData.lifePaths[roll - 1]

    private fun waiting(stage: Stage, draft: Draft, request: ProcessRequest) =
        InteractiveStep.Waiting(State(stage, draft, request), request)

    private fun selected(request: ProcessRequest.Choose, response: ProcessResponse): String {
        require(response is ProcessResponse.Selected && response.optionIds.size == 1)
        return response.optionIds.single().also { id -> require(request.options.any { it.id == id }) }
    }

    private fun entered(request: ProcessRequest.EnterText, response: ProcessResponse): String {
        require(response is ProcessResponse.TextEntered)
        require(request.allowBlank || response.value.isNotBlank())
        return response.value
    }

    private fun rolled(request: ProcessRequest.Roll, response: ProcessResponse): Map<String, Int> {
        require(response is ProcessResponse.Rolled)
        val expected = request.rolls.map(RollSpec::id).toSet()
        require(response.totals.keys == expected)
        request.rolls.forEach { spec ->
            val value = response.totals.getValue(spec.id)
            val range = (spec.dice.count + spec.dice.modifier)..(spec.dice.count * spec.dice.sides + spec.dice.modifier)
            require(value in range)
        }
        return response.totals
    }

    private fun <K, V> MutableMap<K, V>.swap(first: K, second: K) {
        val value = getValue(first)
        this[first] = getValue(second)
        this[second] = value
    }

    private fun <T> MutableList<T>.swap(first: Int, second: Int) {
        val value = this[first]
        this[first] = this[second]
        this[second] = value
    }

    private companion object {
        const val FOUNDLING_ROLL = 18
    }
}
