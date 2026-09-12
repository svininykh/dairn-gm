package io.github.dairn.core

data class ModuleId(val value: String) {
    init {
        require(value.matches(Regex("[a-z0-9]+(?:-[a-z0-9]+)*"))) { "Invalid module id: $value" }
    }

    override fun toString(): String = value
}

data class ModuleInfo(
    val id: ModuleId,
    val version: String,
    val nameKey: String,
)

interface DairnModule {
    val info: ModuleInfo
}

class ModuleRegistry(modules: Iterable<DairnModule>) {
    private val modulesById = modules.associateBy { it.info.id }

    init {
        require(modulesById.size == modules.count()) { "Module ids must be unique" }
    }

    fun all(): List<DairnModule> = modulesById.values.sortedBy { it.info.id.value }

    fun find(id: ModuleId): DairnModule? = modulesById[id]
}

