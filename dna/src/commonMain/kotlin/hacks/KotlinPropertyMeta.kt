package com.github.trilobiitti.trilobite.dna.hacks

import kotlin.reflect.KProperty

/**
 * A hack to handle undoneness of https://youtrack.jetbrains.com/issue/KT-22218
 */
expect class KotlinPropertyMeta(property: Any) {
    val name: String
}

open class DefaultKotlinPropertyMeta(property_: Any) {
    private val property = property_ as KProperty<*>

    val name get() = property.name
}
