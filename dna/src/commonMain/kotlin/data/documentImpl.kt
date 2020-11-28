package com.github.trilobiitti.trilobite.dna.data

/**
 * Simple implementation of [Document] that uses a [MutableMap] instance as storage.
 */
class MapDocument(private val map: MutableMap<DocumentFieldKey, Any> = mutableMapOf()) : Document {
    override fun get(key: DocumentFieldKey): Any? = map[key]

    override fun visit(visitor: DocumentVisitor) {
        for ((k, v) in map) visitor.visitValue(k, v)
    }

    override fun set(key: DocumentFieldKey, value: Any?) {
        if (value === null)
            map.remove(key)
        else
            map[key] = value
    }
}

/**
 * Simple implementation of [FieldAccessor] that just accesses a field of a document by a given key.
 */
open class KeyFieldAccessor<T>(
    val key: DocumentFieldKey
) : FieldAccessor<T> {
    override fun read(document: ReadableDocument): T = document[key] as T

    override fun write(document: Document, value: T) {
        document[key] = value
    }
}
