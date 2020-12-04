package com.github.trilobiitti.trilobite.dna.plugins

import com.github.trilobiitti.trilobite.dna.SequenceBuilder
import com.github.trilobiitti.trilobite.dna.SequentialSequenceBuilder

open class DefaultPlugin(
    private var name: String? = null,
    private val block: SequenceBuilder<*, InitializationStage>.() -> Unit
) : Plugin {
    override fun apply(sequenceBuilder: InitializationSequenceBuilder) {
        block(SequentialSequenceBuilder(sequenceBuilder, emptyList()))
    }

    override fun toString(): String = name ?: super.toString()
}

fun plugin(
    name: String? = null,
    block: SequenceBuilder<*, InitializationStage>.() -> Unit
) = DefaultPlugin(name = name, block = block)
