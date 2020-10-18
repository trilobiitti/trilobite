package com.github.trilobiitti.trilobite.dna

import kotlin.test.*

class DefaultOrderTest {
    private lateinit var order: DefaultOrder<String>

    @BeforeTest
    fun setUp() {
        order = DefaultOrder()
    }

    @Test
    fun shouldOrderLinearlyOrderableThings() {
        order += "1" to "2"

        order["1"] = "a"
        order["2"] = "b"
        order["3"] = "c"

        order += "2" to "3"

        assertEquals(
            listOf("a", "b", "c"),
            order.toLinearList()
        )
    }

    @Test
    fun shouldRemoveItems() {
        order["1"] = "a"
        order["2"] = "b"
        val token = order.register(order.parseKey("3"), "c")

        order += "1" to "2"
        order += "2" to "3"

        assertEquals(
            listOf("a", "b", "c"),
            order.toLinearList()
        )

        token.unregister()

        assertEquals(
            listOf("a", "b"),
            order.toLinearList()
        )
    }

    @Test
    fun shouldNotRemoveItemsTwice() {
        val token = order.register(order.parseKey("1"), "a")

        token.unregister()

        assertFails { token.unregister() }
    }

    @Test
    fun shouldKeepMultipleValuesForTheSameKey() {
        order["1"] = "a"
        order["1"] = "b"
        order["2"] = "c"

        order += "1" to "2"

        val lst = order.toLinearList()

        assertEquals(3, lst.size)
        assertEquals("c", lst[2])
        assertTrue(lst.contains("a"))
        assertTrue(lst.contains("b"))
    }

    @Test
    fun shouldDetectLoops() {
        order["1"] = "a"
        order["2"] = "b"
        order["3"] = "c"

        order += "1" to "2"
        order += "2" to "3"
        order += "3" to "1"

        assertFails { order.validate() }
        assertFails { order.toLinearList() }
        assertFails { order.visit<Unit>({ w, t, f -> Unit }) }
    }

    @Test
    fun shouldDeleteLinks() {
        order["1"] = "a"
        order["2"] = "b"
        order["3"] = "c"

        order += "1" to "2"
        order += "2" to "3"
        val tk = order.link(order.parseKey("3"), order.parseKey("1"))

        assertFails { order.validate() }

        tk.unregister()

        order.validate()

        assertEquals(
            listOf("a", "b", "c"),
            order.toLinearList()
        )
    }

    @Test
    fun shouldNotDeleteLinksTwice() {
        val tk = order.link(order.parseKey("3"), order.parseKey("1"))

        tk.unregister()

        assertFails { tk.unregister() }
    }

    @Test
    fun shouldHandleDuplicateLinks() {
        order["1"] = "a"
        order["2"] = "b"
        order["3"] = "c"

        order += "1" to "2"
        order += "1" to "2"
        order += "2" to "3"
        val tk1 = order.link(order.parseKey("3"), order.parseKey("1"))
        val tk2 = order.link(order.parseKey("3"), order.parseKey("1"))

        assertFails { order.validate() }

        tk1.unregister()

        assertFails { order.validate() }

        tk2.unregister()

        order.validate()

        assertEquals(
            listOf("a", "b", "c"),
            order.toLinearList()
        )
    }
}
