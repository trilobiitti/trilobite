package com.github.trilobiitti.trilobite.dna

import kotlin.test.Test
import kotlin.test.assertEquals

class PathExpressionTest {
    fun e(src: String, exact: Boolean = true): PathExpression<String> =
        PathExpression(IdentityVariable(), src, exact = exact)

    fun d(vararg rules: Pair<PathExpression<String>, String>): Decider<String, String> =
        DefaultDeciderBuilder<String, String>()
            .apply {
                rules.forEach { (expr, item) ->
                    addRule(expr.conditions, item)
                }
            }
            .build<String, String>(
                ContextIndependentImmutableDecisionFactory {
                    it.sorted().joinToString(", ")
                }
            )

    @Test
    fun shouldProduceCorrectConditionsForNonParametrizedPaths() {
        val d = d(e("/a/b/c") to "abc")

        assertEquals("abc", d("/a/b/c"))
        assertEquals("", d("/a/b"))
        assertEquals("", d("/a/b/e"))
        assertEquals("", d("/a/b/c/d"))
        assertEquals("", d("/"))
        assertEquals("", d(""))
        assertEquals("", d("/e"))
    }

    @Test
    fun shouldProduceCorrectConditionsForExpressionsWithParameters() {
        val d = d(e("/a/:b/c") to "abc")

        assertEquals("abc", d("/a/b/c"))
        assertEquals("abc", d("/a/rmagedde/c"))
        assertEquals("", d("/a/b/e"))
        assertEquals("", d("/a/b/c/d"))
        assertEquals("", d("/a/b"))
        assertEquals("", d("/"))
        assertEquals("", d(""))
    }

    @Test
    fun shouldProduceCorrectConditionsForExpressionsWithRegexps() {
        val d = d(e("/a/:(b|c)") to "abc")

        assertEquals("abc", d("/a/b"))
        assertEquals("abc", d("/a/c"))
        assertEquals("", d("/a/d"))
        assertEquals("", d("/a/"))
        assertEquals("", d("/a/d/b"))
        assertEquals("", d("/a/b/c"))
        assertEquals("", d("/"))
    }

    @Test
    fun shouldExtractParameters() {
        val e = e("/:p1/b/:p2")
        val m = mutableMapOf<String, String>()

        e.extractParameters(DefaultDecisionContext("/a/b/c")) { p, v -> m[p] = v }

        assertEquals(mapOf("p1" to "a", "p2" to "c"), m)
    }
}
