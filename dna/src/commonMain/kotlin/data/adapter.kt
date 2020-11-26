package com.github.trilobiitti.trilobite.dna.data

import kotlin.reflect.KProperty

abstract class DocumentAdapter {
    private lateinit var document: Document

    fun init(document: Document) {
        this.document = document
    }

    operator fun <T> getValue(thisRef: DocumentAdapter?, property: KProperty<*>): T {
        println("a.gv ${property::class.simpleName} ${property is KProperty}")

        val reader = DocumentAdapters.fieldReader(property, document) as FieldReader<T>

        return reader.read(document)
    }

    operator fun <T> setValue(thisRef: DocumentAdapter?, property: KProperty<*>, value: T) {
        val writer = DocumentAdapters.fieldWriter(property, document) as FieldWriter<T>

        return writer.write(document, value)
    }
}
