package com.github.trilobiitti.trilobite.dna.plugins

import com.github.trilobiitti.trilobite.dna.SequenceBuilder
import com.github.trilobiitti.trilobite.dna.SequentialSequenceBuilder
import com.github.trilobiitti.trilobite.dna.expect
import com.github.trilobiitti.trilobite.dna.plugins.DefaultPlugin.Companion.END_BARRIER
import com.github.trilobiitti.trilobite.dna.plugins.DefaultPlugin.Companion.START_BARRIER

/**
 * Base class for most of Trilobite [Plugin]s.
 *
 * Provides a simple interface to declare dependencies using [SequenceBuilder] instead of a raw
 * [InitializationSequenceBuilder] provided by plugin loader.
 *
 * Also provides a [START_BARRIER] dependency and [END_BARRIER] reverse dependency, so some special plugins can declare
 * that they must be loaded before/after most of the other plugins.
 */
open class DefaultPlugin(
    private var name: String? = null,
    private val block: SequenceBuilder<*, InitializationStage>.() -> Unit
) : Plugin {
    constructor(
        block: SequenceBuilder<*, InitializationStage>.() -> Unit
    ) : this(null, block)

    override fun apply(sequenceBuilder: InitializationSequenceBuilder) =
        SequentialSequenceBuilder(sequenceBuilder, emptyList()).run {
            expect(START_BARRIER)
            block()
            expect(END_BARRIER)
        }

    override fun toString(): String = name ?: super.toString()

    companion object {
        /**
         * Name of initialization stage that is executed before any stages added by plugins inheriting [DefaultPlugin]
         * (unless those plugins use `independently` sequence builder or something alike).
         *
         * This constant is used by some plugins initializing very low level functionalities of the Trilobite
         * (e.g. [DI][com.github.trilobiitti.trilobite.dna.di.DI] container initialized by
         * [PlainRootDIPlugin][com.github.trilobiitti.trilobite.dna.di.PlainRootDIPlugin]).
         *
         * @see END_BARRIER
         */
        const val START_BARRIER = "default_plugins:start"

        /**
         * Name of initialization stage that is executed after any stages added by plugins inheriting [DefaultPlugin]
         * (unless those plugins use `independently` sequence builder or something alike).
         *
         * @see START_BARRIER
         */
        const val END_BARRIER = "default_plugins:end"
    }
}
