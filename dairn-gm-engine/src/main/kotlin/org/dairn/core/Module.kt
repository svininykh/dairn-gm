package org.dairn.core

data class ModuleId(val value: String) {
    init {
        require(value.matches(Regex("[a-z0-9]+(?:-[a-z0-9]+)*"))) { "Invalid module id: $value" }
    }

    override fun toString(): String = value
}

data class ModuleInfo(
    val id: ModuleId,
    val version: String,
    val name: String,
) {
    init {
        require(version.isNotBlank()) { "Module version cannot be blank" }
        require(name.isNotBlank()) { "Module name cannot be blank" }
    }
}

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
