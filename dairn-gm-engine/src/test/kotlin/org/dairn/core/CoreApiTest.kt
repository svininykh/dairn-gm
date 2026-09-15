package org.dairn.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CoreApiTest {
    @Test
    fun `table uses one-based game indexes`() {
        val table = ListTable(listOf("first", "second"))
        assertEquals("first", table.entry(1))
        assertFailsWith<IllegalArgumentException> { table.entry(0) }
    }

    @Test
    fun `registry sorts modules by stable id`() {
        val second = module("zeta")
        val first = module("alpha")
        assertEquals(listOf(first, second), ModuleRegistry(listOf(second, first)).all())
    }

    private fun module(id: String) = object : DairnModule {
        override val info = ModuleInfo(ModuleId(id), "test", id.replaceFirstChar(Char::uppercase))
    }
}
