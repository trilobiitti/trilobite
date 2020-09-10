package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.di.bindDependency
import kotlin.reflect.KClass
import kotlin.reflect.cast

val convertValue = bindDependency<Any, Any?, KClass<*>>("convert value") { value, clz ->
    throw SimpleValidationError("'$value' ${value?.let { " (of type ${it::class})" }} cannot be converted to $clz")
}

data class ClassType<T : Any>(
        private val clz: KClass<T>
) : DataType<T> {
    override fun isInstance(value: Any?): Boolean = clz.isInstance(value)

    @OptIn(ExperimentalStdlibApi::class)
    @Suppress("UNCHECKED_CAST")
    override fun cast(value: Any?): T = if (clz.isInstance(value))
        value as T
    else
        clz.cast(convertValue(value, clz))

    override fun validate(value: T) {}

    override val baseType: DataType<T>
        get() = this

    override val runtimeClass
        get() = clz
}

data class CollectionType<T, C : Collection<T>>(
        private val itemType: DataType<T>,
        private val collectionType: KClass<out Collection<*>>,
        private val fromList: (List<T>) -> C
) : DataType<C> {
    override val baseType: DataType<C> = if (itemType == itemType.baseType)
        this
    else
        CollectionType(itemType.baseType, collectionType, fromList)

    @OptIn(ExperimentalStdlibApi::class)
    override fun isInstance(value: Any?): Boolean {
        if (!collectionType.isInstance(value))
            return false

        val v = collectionType.cast(value)

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

data class NullableType<T>(
        private val dataType: DataType<T>
) : DataType<T?> {
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

data class EnumType<T>(
        private val enumValues: Set<T>,
        private val valueType: DataType<T>
) : DataType<T> {
    override val baseType: DataType<T>
        get() = valueType.baseType

    private fun validate0(value: T) {
        if (value !in enumValues)
            throw SimpleValidationError(
                    "Value must be one of: ${enumValues.joinToString(", ")}, but not $value"
            )
    }

    override fun validate(value: T) {
        valueType.validate(value)

        validate0(value)
    }

    override fun cast(value: Any?): T = valueType.cast(value).also(this::validate0)

    override val runtimeClass: KClass<*>
        get() = valueType.runtimeClass

    override fun isInstance(value: Any?): Boolean = value in enumValues
}

data class UnionType<T>(
        private val types: List<DataType<out T>>,
        override val baseType: DataType<T>
) : DataType<T> {
    override val runtimeClass: KClass<*>
        get() = baseType.runtimeClass

    override fun cast(value: Any?): T {
        val acc = ValidationError.accumulator()

        for (t in types) acc.collectError {
            return t.cast(value)
        }

        acc.flush()

        error("Unreachable statement reached")
    }

    override fun isInstance(value: Any?): Boolean {
        for (t in types)
            if (t.isInstance(value))
                return true

        return false
    }

    private fun <TT> validateForType(value: T, type: DataType<TT>) {
        type.validate(type.baseType.cast(value))
    }

    override fun validate(value: T) {
        val acc = ValidationError.accumulator()

        for (t in types) acc.collectError {
            validateForType(value, t)

            // Return without error if value is valid value for any of united types
            return
        }

        acc.flush()
    }
}

object DataTypes {
    val STRING = ClassType(String::class)
    val NUMBER = ClassType(Number::class)
    val INT = ClassType(Int::class)
    val LONG = ClassType(Long::class)
    val FLOAT = ClassType(Float::class)
    val DOUBLE = ClassType(Double::class)

    val ANY = ClassType(Any::class)
}
