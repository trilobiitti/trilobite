package com.github.trilobiitti.trilobite.dna.operations

/**
 * Interface of an entry point that causes execution of certain [OperationPlan].
 *
 * For further simplification a [Function] interface could be used for this entity, but a separate interface was created
 * to avoid potential difficulties with implementation of function interface for Operation instances that have to be an
 * object.
 */
interface Operation<TCtx : Any> {
    suspend fun execute(input: TCtx): TCtx
}
