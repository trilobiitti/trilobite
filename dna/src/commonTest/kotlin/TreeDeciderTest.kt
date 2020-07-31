package com.github.trilobiitti.trilobite.dna

import kotlin.test.*

class TreeDeciderTest {
    private data class MapKeyVariable(val key: String): DecisionVariable<Map<String, String>, String> {
        override fun getFrom(context: DecisionContext<Map<String, String>>): String = context.input[key] ?: ""
    }

    fun concatSet(items: Set<String>): String = items.toList().sorted().joinToString(", ")

    fun build(block: DeciderBuilder<Map<String, String>, String>.() -> Unit): Decider<Map<String, String>, String> =
        DefaultDeciderBuilder<Map<String, String>, String>().also(block).build(this::concatSet)

    @Test
    fun `should handle simple conditions`() {
        val d = build {
            add("a is 1") {
                expect(MapKeyVariable("a"), "1")
            }
            add("a is 2") {
                expect(MapKeyVariable("a"), "2")
            }
            add("b is 1") {
                expect(MapKeyVariable("b"), "1")
            }
        }

        assertEquals("", d(mapOf()))
        assertEquals("a is 1", d(mapOf("a" to "1")))
        assertEquals("a is 2", d(mapOf("a" to "2")))
        assertEquals("b is 1", d(mapOf("b" to "1")))
        assertEquals("a is 1, b is 1", d(mapOf("a" to "1", "b" to "1")))
        assertEquals("a is 2, b is 1", d(mapOf("a" to "2", "b" to "1")))
    }

    @Test
    fun `should handle predicate variables`() {
        val d = build {
            add("a is not lowercase") {
                expect(MapKeyVariable("a")) { s -> s.toLowerCase() != s }
            }
            add("a is not uppercase") {
                expect(MapKeyVariable("a")) { s -> s.toUpperCase() != s }
            }
            add("a is UP") {
                expect(MapKeyVariable("a"), "UP")
            }
        }

        assertEquals("a is UP, a is not lowercase", d(mapOf("a" to "UP")))
        assertEquals("a is not uppercase", d(mapOf("a" to "lo")))
        assertEquals("a is not lowercase, a is not uppercase", d(mapOf("a" to "lU")))
    }
}
