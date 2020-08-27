package com.github.trilobiitti.trilobite.dna.data

interface DocumentVisitor<TKey, TValue> {
    fun visitValue(key: TKey, value: TValue)
}

interface BaseReadableDocument<TKey, TValue> {
    operator fun get(key: TKey): TValue?

    fun visit(visitor: DocumentVisitor<TKey, TValue>)
}

interface BaseWritableDocument<TKey, TValue> {
    operator fun set(key: TKey, value: TValue?)
}

interface FieldReader<TKey, TValueBase, TFieldValue> {
    fun read(document: BaseReadableDocument<TKey, TValueBase>): TFieldValue
}

interface FieldWriter<TKey, TValueBase, TFieldValue> {
    fun write(document: BaseWritableDocument<TKey, TValueBase>, value: TFieldValue)
}
