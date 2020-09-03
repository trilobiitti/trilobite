package com.github.trilobiitti.trilobite.dna.data

import kotlin.reflect.KClass

data class ClassType<T : Any>(
        private val clz: KClass<T>,
        private val extendedCast: (Any?) -> T
) : DataType<T> {
    override fun isInstance(value: Any?): Boolean = clz.isInstance(value)

    @Suppress("UNCHECKED_CAST")
    override fun cast(value: Any?): T = if (clz.isInstance(value))
        value as T
    else
        extendedCast(value)

    override fun validate(value: T) {}

    override val baseType: DataType<T>
        get() = this

    override val runtimeClass
        get() = clz
}

data class CollectionType<T, C : Collection<T>>(
        private val itemType: DataType<T>,
        private val collectionType: KClass<C>,
        private val fromList: (List<T>) -> C
) : DataType<C> {
    override val baseType: DataType<C> = if (itemType == itemType.baseType)
        this
    else
        CollectionType(itemType.baseType, collectionType, fromList)

    override fun isInstance(value: Any?): Boolean {
        if (!collectionType.isInstance(value))
            return false

        val v = value as Collection<*>

        for (i in v)
            if (!itemType.isInstance(i))
                return false

        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun cast(value: Any?): C {
        val v = value as? Collection<*> ?: throw SimpleValidationError("Value is not a collection")

        if (isInstance(v)) return v as C

        return fromList(v.map { itemType.cast(it) })
    }

    override fun validate(value: C) {
        val errors = ValidationError.accumulator()

        for ((i, v) in value.withIndex())
            errors.collectErrorFor(i) {
                itemType.validate(v)
            }

        errors.flush()
    }

    override val runtimeClass
        get() = collectionType
}

data class NullableType<T>(val dataType: DataType<T>) : DataType<T?> {
    override fun isInstance(value: Any?): Boolean = value === null || dataType.isInstance(value)

    override fun cast(value: Any?): T? = value?.let { dataType.cast(it) }

    override fun validate(value: T?) {
        value?.let { dataType.validate(it) }
    }

    override val baseType: DataType<T?> = if (dataType == dataType.baseType)
        this
    else
        NullableType(dataType.baseType)

    override val runtimeClass
        get() = baseType.runtimeClass
}
