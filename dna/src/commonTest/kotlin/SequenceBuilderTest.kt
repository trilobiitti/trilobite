package com.github.trilobiitti.trilobite.dna

import kotlin.test.Test
import kotlin.test.assertEquals

class SequenceBuilderTest {
    fun test(block: SequenceBuilder<*, String>.() -> Unit): List<String> {
        val o = DefaultOrder<String>()

        SequentialSequenceBuilder(o, emptyList()).run(block)

        return o.toLinearList()
    }

    @Test
    fun shouldOrderSequentialItems() {
        assertEquals(
            listOf("foo", "bar", "baz", "buz"),
            test {
                add("foo")
                add("kBar", "bar")
                seq {
                    add("baz")
                    add("buz")
                }
            }
        )
    }

    @Test
    fun shouldComposeParallelOrders() {
        assertEquals(
            listOf("foo", "prebar", "bar", "postbar", "baz"),
            test {
                add("foo")

                par {
                    seq {
                        expect("kbar")

                        add("postbar")
                    }

                    seq {
                        add("prebar")
                        add("kbar", "bar")
                    }
                }

                add("baz")
            }
        )
    }

    @Test
    fun shouldHandleIndependentSubsequences() {
        assertEquals(
            listOf("foo", "bar", "baz"),
            test {
                add("kbar", "bar")

                independently {
                    add("foo")

                    expect("kbar")
                }

                add("baz")
            }
        )
    }
}
