package com.github.trilobiitti.trilobite.dna.data

import kotlin.reflect.KClass

class SchemaFieldImpl<T>(
        override val type: DataType<T>,
        override val key: DocumentFieldKey
) : SchemaField<T> {
    override fun read(document: ReadableDocument): T = type.cast(document[key])

    override fun write(document: Document, value: T) {
        document[key] = type.cast(value)
    }
}

private val DOCUMENT_TYPE = ClassType(Document::class) { TODO("Deal with 'extendedCast'...") }

class SchemaImpl(
        fields: Iterable<SchemaField<*>>
) : Schema {
    override val fields: Map<DocumentFieldKey, SchemaField<*>> = fields.map { it.key to it }.toMap()
    override val baseType get() = DOCUMENT_TYPE
    override val runtimeClass: KClass<*> get() = Document::class

    override fun cast(value: Any?): Document {
        val doc = DOCUMENT_TYPE.cast(value)
        val errors = ValidationError.accumulator()

        for ((k, f) in fields) {
            errors.collectErrorFor(k) {
                val fv = doc[k]
                val cv = f.type.cast(fv)

                if (cv != fv) doc[k] = cv
            }
        }

        errors.flush()

        return doc
    }

    override fun isInstance(value: Any?): Boolean {
        if (value !is Document)
            return false

        for ((k, f) in fields) {
            if (!f.type.isInstance(value[k]))
                return false
        }

        return true
    }

    override fun validate(value: Document) {
        val errors = ValidationError.accumulator()

        errors.collectError { DOCUMENT_TYPE.validate(value) }

        for ((k, f) in fields)
            errors.collectErrorFor(k) {
                f.type.cast(value[k])
            }

        errors.flush()
    }
}
