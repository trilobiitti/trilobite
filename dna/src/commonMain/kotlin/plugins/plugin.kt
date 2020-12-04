package com.github.trilobiitti.trilobite.dna.plugins

import com.github.trilobiitti.trilobite.dna.OrderBuilder

/**
 * Function that performs part of application initialization sequence.
 *
 * Initialization stages are expected to be transactional - they should either do what they should do and exit without
 * error or do nothing and fail with error.
 *
 * @see [Plugin]
 */
typealias InitializationStage = suspend () -> Unit

/**
 * Builder that builds an application initialization sequence.
 *
 * Application initialization sequence is implemented as [Order][com.github.trilobiitti.trilobite.dna.Order], thus
 * it's builder is a [OrderBuilder].
 */
typealias InitializationSequenceBuilder = OrderBuilder<*, InitializationStage>

/**
 * Plugin represents a set of functionalities added to the application.
 *
 * Functionalities are added by [InitializationStage]s which are functions executed in order defined by initialization
 * sequence.
 * A plugin can add some [InitializationStage]s to initialization sequence in it's [apply] method using provided
 * [InitializationSequenceBuilder].
 *
 * It is called initialization sequence but it is not much related to application initialization and sometimes it is not
 * a sequence.
 * In fact "initialization sequence" represents a set of actions used to initialize some set of application
 * functionalities, potentially implemented by multiple plugins.
 * These actions may be performed sequentially but some actions may be executed concurrently if application
 * implementation supports concurrent execution and action dependencies graph allows it.
 *
 * A typical [InitializationStage] registers some dependencies in [DI container][com.github.trilobiitti.trilobite.dna.di.DI]
 * or interacts with things already registered there.
 * However, it is not limited to interactions with DI container only.
 */
interface Plugin {
    /**
     * Adds [InitializationStage]s to initialization sequence.
     *
     * @param sequenceBuilder initialization sequence builder
     */
    fun apply(sequenceBuilder: InitializationSequenceBuilder)
}
