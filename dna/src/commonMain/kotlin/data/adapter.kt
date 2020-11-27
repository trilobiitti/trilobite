package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.hacks.KotlinPropertyMeta

abstract class DocumentAdapter {
    private lateinit var document: Document

    fun init(document: Document) {
        this.document = document
    }

    operator fun <T> getValue(thisRef: DocumentAdapter?, property: Any): T {
        val reader = DocumentAdapters.fieldReader(KotlinPropertyMeta(property), document) as FieldReader<T>

        return reader.read(document)
    }

    operator fun <T> setValue(thisRef: DocumentAdapter?, property: Any, value: T) {
        val writer = DocumentAdapters.fieldWriter(KotlinPropertyMeta(property), document) as FieldWriter<T>

        return writer.write(document, value)
    }
}
