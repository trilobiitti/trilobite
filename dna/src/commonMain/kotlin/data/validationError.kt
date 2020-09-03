package com.github.trilobiitti.trilobite.dna.data

abstract class ValidationError : Exception() {
    override val message: String?
        get() = StringBuilder().also { format(it) }.toString()

    abstract fun format(dst: Appendable, path: String = "")

    companion object {
        private val TOP_LEVEL_ERROR_SENTINEL = object {}

        class Accumulator {
            private var v: MutableMap<Any, MutableList<ValidationError>>? = null

            private fun map() = v ?: mutableMapOf<Any, MutableList<ValidationError>>().also { v = it }

            fun add(key: Any, error: ValidationError) {
                map().getOrPut(key) { mutableListOf() }.add(error)
            }

            fun add(error: ValidationError) = add(TOP_LEVEL_ERROR_SENTINEL, error)

            fun flush() {
                val v = this.v
                if (v === null) return

                val topLevelErrors = v[TOP_LEVEL_ERROR_SENTINEL] ?: emptyList()
                v.remove(TOP_LEVEL_ERROR_SENTINEL)

                if (v.isEmpty() && topLevelErrors.size == 1)
                    throw topLevelErrors[0]

                throw CompositeValidationError(v, topLevelErrors)
            }

            inline fun collectErrorFor(key: Any, block: () -> Unit) {
                try {
                    block()
                } catch (e: ValidationError) {
                    add(key, e)
                }
            }

            inline fun collectError(block: () -> Unit) {
                try {
                    block()
                } catch (e: ValidationError) {
                    add(e)
                }
            }
        }

        fun accumulator() = Accumulator()
    }
}

/**
 * Composition of multiple validation errors.
 *
 * May represent multiple errors in a single value e.g.:
 * "
 * The password must contain:
 *  - a capital letter
 *  - two numbers
 *  - a symbol
 *  - an inspiring message
 *  - a spell
 *  - a gang sign
 *  - a hieroglyph
 *  - the blood of a virgin
 * "
 * which will result in multiple [topLevelErrors], multiple errors in components of composite data structure e.g.:
 * "
 *  - "field.roses"
 *      - must be red
 *  - "field.violets"
 *      - must be blue
 * "
 * which will result in error lists for each component in [errors].
 * Or both errors of separate components and overall errors in whole structure.
 *
 * TODO: Current structure is ambiguous: errors may be listed as field errors or as topLevelErrors of CompositeValidationError for the field
 */
class CompositeValidationError(
        private val errors: Map<Any, List<ValidationError>>,
        private val topLevelErrors: List<ValidationError>
) : ValidationError() {
    override fun format(dst: Appendable, path: String) {
        for (error in topLevelErrors)
            error.format(dst, path)

        for ((key, errors) in errors) {
            val nestedPath = "$path[$key]"
            for (error in errors)
                error.format(dst, nestedPath)
        }
    }
}

class SimpleValidationError(
        private val msg: String
) : ValidationError() {
    override fun format(dst: Appendable, path: String) {
        dst
                .append(path)
                .append(msg.replace("\n", " ".repeat(path.length)))
                .append('\n')
    }
}
