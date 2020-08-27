package com.github.trilobiitti.trilobite.dna.data

import kotlin.reflect.KClass

/**
 * Represents a value type with (probably) some additional constraints.
 *
 * @param T type values of this type are represented by in runtime
 */
interface DataType<T : Any?> {
    /**
     * Checks if [value] belongs to this type.
     */
    fun isInstance(value: Any?): Boolean

    /**
     * Converts value to this type.
     *
     * @throws IllegalArgumentException if the value cannot be converted to this type
     */
    fun cast(value: Any?): T

    /**
     * Checks if value of type [T] belongs to this type.
     *
     * TODO: Implement a separate ValidationException class
     * @throws IllegalArgumentException if the value doesn't match any constraints this type specifies
     */
    fun validate(value: T)

    /**
     * A [DataType] with the same runtime type such as any value of type [T] is valid value for [baseType].
     *
     * [baseType]'s of any [DataType]'s with the same [T] must be equal. [baseType]'s of any [DataType]'s with different
     * [T] must not be equal.
     */
    val baseType: DataType<T>

    /**
     * A class values of type [T] are represented by in runtime.
     *
     * TODO: It would be nice to somehow specify that it's not KClass<*> but KClass<(without nullability)T> but I can't
     */
    val runtimeClass: KClass<*>
}

