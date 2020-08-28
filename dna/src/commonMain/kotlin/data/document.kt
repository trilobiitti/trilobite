package com.github.trilobiitti.trilobite.dna.data

inline class StringFieldKey(val str: String)

/**
 * Type of document field names.
 *
 * Currently is implemented as alias for a inline class wrapping a string. Later may be changed to an interface
 * if non-string field names will be found as useful.
 */
typealias DocumentFieldKey = StringFieldKey

/**
 * Visitor for [ReadableDocument].
 */
interface DocumentVisitor {
    /**
     * Called for each present field of a visited document.
     */
    fun visitValue(key: DocumentFieldKey, value: Any)
}

/**
 * Part of [Document] interface that includes read operations only.
 *
 * In all cases where read only operations are used on document it should be represented as [ReadableDocument].
 */
interface ReadableDocument {
    /**
     * Reads value of given field.
     *
     * @return value of requested field or `null` iff the field is not present in the document.
     */
    operator fun get(key: DocumentFieldKey): Any?

    /**
     * Lets a [visitor] visit this document.
     */
    fun visit(visitor: DocumentVisitor)
}

/**
 * Part of [Document] interface that includes write operations only.
 *
 * In all cases where write only operations are used on document it should be represented as [ReadableDocument].
 */
interface WritableDocument {
    /**
     * Sets given field of document to a given value.
     *
     * If [value] is `null`, the field will be removed from document.
     */
    operator fun set(key: DocumentFieldKey, value: Any?)
}

/**
 * Main interface of a document.
 *
 * Document is a key-value storage that represents attributes of certain entity.
 *
 * You may think of it as of an JSON object, except the way `null`s are handled. Unlike JSON/JavaScript, value of the a
 * document field cannot be `null`. If [ReadableDocument.get] returns `null` for given field name then the field is
 * considered as missing in the document.
 *
 * As long as this interface doesn't declare any own methods, any class that implements both [ReadableDocument]
 * and [WritableDocument] must also implement [Document].
 */
interface Document : ReadableDocument, WritableDocument

/**
 * Object that reads a value of type [T] from a [ReadableDocument].
 *
 * A [FieldReader] may read just a single field of a document or create a value based on values of few document fields
 * (including fields of nested documents).
 */
interface FieldReader<out T : Any> {
    /**
     * Reads a value from document.
     */
    fun read(document: ReadableDocument): T
}

/**
 * Object that writes a value of type [T] to a document.
 *
 * A [FieldWriter] may just write a single field of document or write values of few document fields including fields of
 * nested documents.
 */
interface FieldWriter<in T : Any> {
    /**
     * Writes field of a document.
     */
    fun write(document: Document, value: T)
}

/**
 * Object that both reads and writes a value of type [T] from/to a document.
 */
interface FieldAccessor<T : Any> : FieldReader<T>, FieldWriter<T>
