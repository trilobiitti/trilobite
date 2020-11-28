package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.hacks.KotlinPropertyMeta

abstract class DocumentAdapter {
    private lateinit var document: Document

    fun init(document: Document) {
        this.document = document
    }

    operator fun <T> getValue(thisRef: DocumentAdapter?, property: Any): T = getValue(document, property)

    operator fun <T> setValue(thisRef: DocumentAdapter?, property: Any, value: T) = setValue(document, property, value)

    companion object {
        operator fun <T> getValue(self: ReadableDocument, property: Any): T {
            val reader = DocumentAdapters.fieldReader(KotlinPropertyMeta(property), self) as FieldReader<T>

            return reader.read(self)
        }

        operator fun <T> setValue(self: Document, property: Any, value: T) {
            val writer = DocumentAdapters.fieldWriter(KotlinPropertyMeta(property), self) as FieldWriter<T>

            return writer.write(self, value)
        }
    }
}
