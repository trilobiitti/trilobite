package com.github.trilobiitti.trilobite.dna.serialization

import com.github.trilobiitti.json.parsers.lazy.LazyJsonParser
import com.github.trilobiitti.trilobite.dna.data.Document
import com.github.trilobiitti.trilobite.dna.data.DocumentFieldKey
import com.github.trilobiitti.trilobite.dna.data.DocumentVisitor
import com.github.trilobiitti.trilobite.dna.data.Documents

/**
 * Deserialized JSON string into tree of [Document]s, [MutableList]s and values.
 */
open class LazyJsonDocumentParser : LazyJsonParser<
    Document,
    MutableList<Any?>,
    Document,
    MutableList<Any?>
    >() {
    override fun startObject(): Document = Documents.newEmptyDocument()

    override fun addField(builder: Document, fieldName: String, value: Any?): Document = builder.also {
        it[DocumentFieldKey(fieldName)] = value
    }

    override fun endObject(builder: Document): Document = builder

    private inner class LazyDocument(
        private var string: String?,
        private val spanStart: Int,
        private val spanEnd: Int
    ) : Document {
        private val ld by lazy {
            val d = parseObject(string!!, spanStart, spanEnd)

            string = null

            return@lazy d
        }

        override fun get(key: DocumentFieldKey): Any? = ld[key]

        override fun visit(visitor: DocumentVisitor) = ld.visit(visitor)

        override fun set(key: DocumentFieldKey, value: Any?) {
            ld[key] = value
        }

        override fun toString(): String = "LazyDocument($ld)"
    }

    override fun lazyObject(string: String, spanStart: Int, spanEnd: Int): Document = LazyDocument(
        string, spanStart, spanEnd
    )

    override fun startArray(): MutableList<Any?> = mutableListOf()

    override fun addElement(builder: MutableList<Any?>, value: Any?): MutableList<Any?> = builder.apply { add(value) }

    override fun endArray(builder: MutableList<Any?>): MutableList<Any?> = builder

    private inner class LazyMutableList(
        var string: String?,
        val spanStart: Int,
        val spanEnd: Int
    ) : MutableList<Any?> {
        val ll by lazy {
            val l = parseArray(string!!, spanStart, spanEnd)

            string = null

            return@lazy l
        }

        override val size: Int
            get() = ll.size

        override fun contains(element: Any?): Boolean = ll.contains(element)
        override fun containsAll(elements: Collection<Any?>): Boolean = ll.containsAll(elements)
        override fun get(index: Int): Any? = ll.get(index)
        override fun indexOf(element: Any?): Int = ll.indexOf(element)
        override fun isEmpty(): Boolean = ll.isEmpty()
        override fun iterator(): MutableIterator<Any?> = ll.iterator()
        override fun lastIndexOf(element: Any?): Int = ll.lastIndexOf(element)
        override fun add(element: Any?): Boolean = ll.add(element)
        override fun add(index: Int, element: Any?) = ll.add(index, element)
        override fun addAll(index: Int, elements: Collection<Any?>): Boolean = ll.addAll(index, elements)
        override fun addAll(elements: Collection<Any?>): Boolean = ll.addAll(elements)
        override fun clear() = ll.clear()
        override fun listIterator(): MutableListIterator<Any?> = ll.listIterator()
        override fun listIterator(index: Int): MutableListIterator<Any?> = ll.listIterator(index)
        override fun remove(element: Any?): Boolean = ll.remove(element)
        override fun removeAll(elements: Collection<Any?>): Boolean = ll.removeAll(elements)
        override fun removeAt(index: Int): Any? = ll.removeAt(index)
        override fun retainAll(elements: Collection<Any?>): Boolean = ll.retainAll(elements)
        override fun set(index: Int, element: Any?): Any? = ll.set(index, element)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Any?> = ll.subList(fromIndex, toIndex)

        override fun equals(other: Any?): Boolean = ll == other
        override fun hashCode(): Int = ll.hashCode()

        override fun toString(): String = "LazyMutableList($ll)"
    }

    override fun lazyArray(string: String, spanStart: Int, spanEnd: Int): MutableList<Any?> = LazyMutableList(
        string, spanStart, spanEnd
    )
}
