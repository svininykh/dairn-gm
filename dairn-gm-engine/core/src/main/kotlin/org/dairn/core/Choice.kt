package org.dairn.core

sealed interface Choice<out T> {
    data class Required<T>(
        val id: String,
        val options: List<T>,
        val minimum: Int = 1,
        val maximum: Int = 1,
    ) : Choice<T> {
        init {
            require(id.isNotBlank()) { "Choice id cannot be blank" }
            require(options.isNotEmpty()) { "A choice needs options" }
            require(minimum in 0..options.size) { "Invalid minimum selection count" }
            require(maximum in minimum..options.size) { "Invalid maximum selection count" }
        }
    }

    data class Resolved<T>(val values: List<T>) : Choice<T>
}

