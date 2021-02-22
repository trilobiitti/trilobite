package com.github.trilobiitti.trilobite.dna.serialization

import com.github.trilobiitti.trilobite.dna.data.Document
import com.github.trilobiitti.trilobite.dna.data.DocumentFieldKey
import kotlin.test.Test
import kotlin.test.assertEquals

class LazyJsonDocumentDeserializerTest {
    private val json = LazyJsonDocumentParser()

    @Test
    fun shouldParseDocumentFromJson() {
        val d = json.parse(
            """
            {"foo": "bar"}
            """.trimIndent()
        )

        if (d !is Document) {
            error("Result must be a document")
        }

        assertEquals(
            "bar",
            d[DocumentFieldKey("foo")]
        )
    }
}
