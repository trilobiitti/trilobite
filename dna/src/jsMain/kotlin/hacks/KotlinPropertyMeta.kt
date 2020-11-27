package com.github.trilobiitti.trilobite.dna.hacks

actual class KotlinPropertyMeta actual constructor(property: Any) {
    private val property: dynamic = property

    actual val name: String
        get() = property.callableName.toString()
}
