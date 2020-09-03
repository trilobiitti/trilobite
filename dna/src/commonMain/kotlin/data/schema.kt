package com.github.trilobiitti.trilobite.dna.data

/**
 * Describes constraints on single field of a document matching a [Schema].
 */
interface SchemaField<T> : FieldAccessor<T> {
    /**
     * Type of this field.
     */
    val type: DataType<T>

    /**
     * Name of the field.
     */
    val key: DocumentFieldKey
}

/**
 * Schema describes a set of [Document]s with specific structure.
 *
 * Inherits conversion/validation methods from [DataType].
 */
interface Schema : DataType<Document> {
    /**
     * Returns set of fields supported by this schema.
     *
     * The fields are stored as [Map] with field name as a key as it is the most convenient format for some important
     * use-cases. Key for any non-null `value` in returned map must be `value.key`.
     *
     * @see SchemaField
     */
    val fields: Map<DocumentFieldKey, SchemaField<*>>
}
