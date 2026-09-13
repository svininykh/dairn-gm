package org.dairn.core

data class InventoryEntry(
    val id: String,
    val slots: Int,
) {
    init {
        require(id.isNotBlank()) { "Inventory entry id cannot be blank" }
        require(slots >= 0) { "Inventory entry slots cannot be negative" }
    }
}

/** Ruleset-neutral snapshot of inventory load. */
data class InventoryLoad(
    val capacity: Int,
    val entries: List<InventoryEntry>,
) {
    init {
        require(capacity > 0) { "Inventory capacity must be positive" }
        require(entries.map(InventoryEntry::id).distinct().size == entries.size) {
            "Inventory entry ids must be unique"
        }
        require(occupiedSlots <= capacity) { "Inventory load exceeds capacity" }
    }

    val occupiedSlots: Int get() = entries.sumOf(InventoryEntry::slots)
    val freeSlots: Int get() = capacity - occupiedSlots
    val isFull: Boolean get() = occupiedSlots == capacity
}
