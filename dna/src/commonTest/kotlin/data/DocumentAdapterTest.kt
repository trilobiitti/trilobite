package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.testHelpers.DIAwareTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestAdapter : DocumentAdapter() {
    val foo: Int by this

    var bar: String by this
}

class DocumentAdapterTest : DIAwareTest {
    override fun initDependencies() {
        DocumentAdapters.fieldAccessor.use { prop, _ ->
            KeyFieldAccessor<Any>(DocumentFieldKey("field_${prop.name}"))
        }
    }

    @Test
    fun shouldReadField() {
        val doc = MapDocument(
            mutableMapOf(
                DocumentFieldKey("field_foo") to 42,
                DocumentFieldKey("foo") to 41
            )
        )

        val a = TestAdapter().apply { init(doc) }

        assertEquals(42, a.foo)
    }

    @Test
    fun shouldReadAndWriteField() {
        val doc = MapDocument(
            mutableMapOf(
                DocumentFieldKey("field_bar") to "42",
                DocumentFieldKey("bar") to "41"
            )
        )

        val a = TestAdapter().apply { init(doc) }

        assertEquals("42", a.bar)

        a.bar = "and what is the question?"

        assertEquals(
            "and what is the question?",
            doc[DocumentFieldKey("field_bar")]
        )
        assertEquals(
            doc[DocumentFieldKey("field_bar")],
            a.bar
        )
    }
}
