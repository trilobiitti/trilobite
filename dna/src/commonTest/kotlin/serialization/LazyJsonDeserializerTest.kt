package com.github.trilobiitti.trilobite.dna.serialization

import kotlin.test.Test
import kotlin.test.assertEquals

class LazyJsonDeserializerTest {
    private val json = ImmutablePrimitiveLazyJsonDeserializer()

    @Test
    fun shouldParseEmptyStrings() {
        assertEquals(
            "",
            json.parse("\"\"")
        )
    }

    @Test
    fun shouldParseSimpleStrings() {
        assertEquals(
            "hello world",
            json.parse("\"hello world\"")
        )
        assertEquals(
            "hello world",
            json.parse("\n\n  \"hello world\" \n\t")
        )
    }

    @Test
    fun shouldParseStringsWithSimpleEscapes() {
        assertEquals(
            "\r\n\t\\\"/\b\u000C",
            json.parse(
                """
                    "\r\n\t\\\"\/\b\f"
                """
            )
        )
    }

    @Test
    fun shouldParseStringsWithHexEscapes() {
        assertEquals(
            "\uFAF0",
            json.parse(
                """
                    "\uFaf0"
                """
            )
        )
    }

    @Test
    fun shouldParseIntegers() {
        assertEquals(
            0,
            (json.parse("0") as Number).toInt()
        )
        assertEquals(
            100,
            (json.parse("100") as Number).toInt()
        )
        assertEquals(
            -42,
            (json.parse("-42") as Number).toInt()
        )
    }

    @Test
    fun shouldParseSimpleFloats() {
        assertEquals(
            0.0,
            (json.parse("0.0") as Number).toDouble()
        )
        assertEquals(
            100.001,
            (json.parse("100.001") as Number).toDouble()
        )
        assertEquals(
            -3.14,
            (json.parse("-3.14") as Number).toDouble()
        )
    }

    @Test
    fun shouldParseExponentialFloats() {
        assertEquals(
            1e100,
            (json.parse("1e100") as Number).toDouble()
        )
        assertEquals(
            1e100,
            (json.parse("1E+100") as Number).toDouble()
        )
        assertEquals(
            1.2e-3,
            (json.parse("1.200E-3") as Number).toDouble()
        )
        assertEquals(
            -3.14E-20,
            (json.parse("-3.14e-20") as Number).toDouble()
        )
    }

    @Test
    fun shouldParseEmptyArrays() {
        assertEquals(
            emptyList<Any?>(),
            json.parse(
                """
                   [] 
                """
            )
        )
        assertEquals(
            emptyList<Any?>(),
            json.parse(
                """
                   [  ] 
                """
            )
        )
        assertEquals(
            emptyList<Any?>(),
            json.parse("[]")
        )
    }

    @Test
    fun shouldParseArrays() {
        assertEquals(
            listOf("foo", "bar", "baz"),
            json.parse(
                """
                   ["foo","bar","baz"] 
                """
            )
        )
        assertEquals(
            listOf("foo", "bar", "baz"),
            json.parse(
                """
                   [ "foo" , "bar" , "baz" ] 
                """
            )
        )
    }

    @Test
    fun shouldParseNestedArrays() {
        assertEquals(
            listOf(listOf(listOf("foo", "bar", "baz"))),
            json.parse(
                """
                   [[["foo","bar","baz"]]] 
                """
            )
        )
        assertEquals(
            listOf(listOf(listOf("foo"), "bar"), "baz"),
            json.parse(
                """
                   [ [["foo" ]
                   , "bar"] , "baz" ] 
                """
            )
        )
    }

    @Test
    fun shouldParseEmptyObjects() {
        assertEquals(
            emptyMap<String, Any?>(),
            json.parse(
                """
                    {}
                """
            )
        )
        assertEquals(
            emptyMap<String, Any?>(),
            json.parse(
                """
                    {   }
                """
            )
        )
        assertEquals(
            emptyMap<String, Any?>(),
            json.parse("{}")
        )
    }

    @Test
    fun shouldParseObjects() {
        assertEquals(
            mapOf(
                "foo" to "bar"
            ),
            json.parse(
                """
                    {"foo":"bar"}
                """
            )
        )
        assertEquals(
            mapOf(
                "foo" to "bar",
                "bar" to "buz"
            ),
            json.parse(
                """
                    {"foo":"bar","bar":"buz"}
                """
            )
        )
    }

    @Test
    fun shouldParseNestedObjects() {
        assertEquals(
            mapOf(
                "foo" to mapOf(
                    "bar" to mapOf(
                        "baz" to "buz"
                    )
                )
            ),
            json.parse(
                """
                    {"foo":{"bar":{"baz":"buz"}}}
                """
            )
        )
        assertEquals(
            mapOf(
                "foo" to mapOf(
                    "bar" to mapOf(
                        "baz" to "buz"
                    )
                )
            ),
            json.parse(
                """
                    { "foo" : { "bar" : { "baz" : "buz" }
                            } } 
                """
            )
        )
    }

    @Test
    fun shouldParseComplexNestedStructures() {
        assertEquals(
            mapOf(
                "foo" to listOf(42.0, true),
                "bar" to mapOf(
                    "baz" to emptyList<Any?>()
                )
            ),
            json.parse(
                """
                   {"foo":[42,true], "bar": {"baz": [ ]}} 
                """
            )
        )
    }

    @Test
    fun shouldParseBooleanValues() {
        assertEquals(
            listOf(true),
            json.parse("[true]")
        )
        assertEquals(
            listOf(true),
            json.parse("[ true ]")
        )
        assertEquals(
            listOf(false),
            json.parse("[false]")
        )
        assertEquals(
            listOf(false),
            json.parse("[ false ]")
        )
    }

    @Test
    fun shouldParseNulls() {
        assertEquals(
            null,
            json.parse("null")
        )
        assertEquals(
            mapOf("foo" to null),
            json.parse("""{ "foo": null }""")
        )
    }
}
