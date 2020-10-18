package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.testHelpers.DIAwareTest
import kotlin.test.*

class SchemaTest : DIAwareTest {
    private val sch = SchemaImpl(
        listOf(
            SchemaFieldImpl(DataTypes.STRING, DocumentFieldKey("name")),
            SchemaFieldImpl(DataTypes.NUMBER, DocumentFieldKey("age"))
        )
    )

    @Test
    fun shouldAcceptDocumentsMatchingTheSchema() {
        val doc = MapDocument(
            mutableMapOf(
                DocumentFieldKey("name") to "John",
                DocumentFieldKey("name") to "Doe",
                DocumentFieldKey("age") to 42
            )
        )

        assertSame(doc, sch.cast(doc))
        assertTrue(sch.isInstance(doc))
        sch.validate(doc)
    }

    @Test
    fun shouldNotAcceptDocumentsNotMatchingTheSchema() {
        val doc = MapDocument(
            mutableMapOf(
                DocumentFieldKey("name") to "Jane",
                DocumentFieldKey("name") to "Doe",
                DocumentFieldKey("age") to "Not your business"
            )
        )

        assertFailsWith(ValidationError::class) {
            sch.cast(doc)
        }

        assertFailsWith(ValidationError::class) {
            sch.validate(doc)
        }

        assertFalse(sch.isInstance(doc))
    }

    @Test
    fun shouldNotAcceptNonDocumentValues() {
        assertFailsWith(ValidationError::class) {
            sch.cast("""{"name": "John", "age": 42}""")
        }

        assertFalse(sch.isInstance("""{"name": "John", "age": 42}"""))
    }
}
