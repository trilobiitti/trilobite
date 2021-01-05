package com.github.trilobiitti.trilobite.dna.operations

import com.github.trilobiitti.trilobite.dna.di.CompositeKey
import com.github.trilobiitti.trilobite.dna.di.Registry
import com.github.trilobiitti.trilobite.dna.di.bindDependency

/**
 * Helper class to store set of named operations with same context type and some share semantics in DI container.
 */
open class OperationRegistry<TCtx : Any, TStage>(
    registryKey: Any,
    registrationKey: Any = CompositeKey(registryKey, REGISTER_OP_SENTINEL),
    editKey: Any = CompositeKey(registryKey, EDIT_OP_SENTINEL),
    executorKey: Any = CompositeKey(registryKey, OP_EXECUTOR_SENTINEL)
) {
    companion object {
        val REGISTER_OP_SENTINEL = object {}

        val EDIT_OP_SENTINEL = object {}

        val OP_EXECUTOR_SENTINEL = object {}
    }

    private val registry: Registry<Operation<TCtx>> = Registry.new(registryKey)

    val register = bindDependency(registrationKey, registry.register)

    val resolve = bindDependency(registryKey, registry.resolve)

    val edit = bindDependency<Unit, Any, OperationPlanEditor<TCtx, TStage>.() -> Unit>(
        editKey
    )

    val executor = bindDependency<OperationPlanExecutor<TCtx, TStage>>(executorKey)

    fun useDefaults() {
        register.useDefault()
        resolve.useDefault()
    }
}
