package com.github.trilobiitti.trilobite.dna.operations

import com.github.trilobiitti.trilobite.dna.Decider
import com.github.trilobiitti.trilobite.dna.DecisionCondition
import com.github.trilobiitti.trilobite.dna.Order
import com.github.trilobiitti.trilobite.dna.OrderBuilder

/**
 * A function that modifies sequence of stages to be performed by an operation at certain conditions.
 */
typealias CasePlanPatch<TStage> = OrderBuilder<*, TStage>.() -> Unit

/**
 * Provides interface to modify [OperationPlan] for a certain operation.
 */
interface OperationPlanEditor<TCtx : Any, TStage> {
    /**
     * Adds a patch to case plans matching given set of conditions.
     *
     * The changes can be applied at any moment after this method is called, so in some rare cases it is important to
     * control order of calls to keep [OperationPlan] state consistent.
     */
    fun applyConditionalPatch(
        condition: Iterable<DecisionCondition<TCtx, *>>,
        planPatch: CasePlanPatch<TStage>
    )

    /**
     * Notifies [OperationPlanEditor] that changes added by preceding calls (e.g. [applyConditionalPatch] calls) should
     * be now applied to actual plan of operation.
     */
    fun flush()
}

/**
 * A set and sequence of actions that can be performed on an object (called context, `TCtx`) depending on it's value.
 *
 * This type is a composition of [Decider] and [Order], where [Decider] chooses a [Order] (partially ordered finite
 * sequence) of actions (also called stages, `TStage`).
 */
typealias OperationPlan<TCtx, TStage> = Decider<TCtx, Order<*, TStage>>

/**
 * Service that executes [OperationPlan]s for provided contexts.
 *
 * Depending on implementation, it may accept different types of contexts and stages, may execute stages sequentially or
 * concurrently, may apply some modifications to context, plan and stages, etc.
 */
interface OperationPlanExecutor<TCtx : Any, TStage> {
    /**
     * Executes a case plan for given context.
     *
     * @param stages sequence of stages to execute
     * @param context the input context
     * @return the output context
     */
    suspend fun executeCase(
        stages: Order<*, TStage>,
        context: TCtx
    ): TCtx

    /**
     * Executes [OperationPlan] for given context.
     *
     * Default implementation picks a case plan and calls [executeCase].
     *
     * TODO: This probably should be moved to an extension function or [Operation] implementation or be a single function of [OperationPlanExecutor] since two separate [executeCase] and [execute] may cause difficulties with implementation of wrappers/decorators for [OperationPlanExecutor]s
     */
    suspend fun execute(
        plan: OperationPlan<TCtx, TStage>,
        context: TCtx
    ): TCtx = executeCase(
        plan.invoke(context),
        context
    )
}

/**
 * Common for some types of [OperationPlanExecutor]s type - a suspending function that executes a stage for given
 * context.
 */
typealias StageExecutor<TStage, TCtx> = suspend (stage: TStage, context: TCtx) -> TCtx

/**
 * Common for some types of [OperationPlanExecutor]s type - a synchronous function that executes a stage for given
 * context.
 */
typealias SynchronousStageExecutor<TStage, TCtx> = (stage: TStage, context: TCtx) -> TCtx

/**
 * Common for some types of [OperationPlanExecutor]s type - a function that merges two output contexts of two sets of
 * stages executed concurrently.
 */
typealias ContextReducer<TCtx> = (ctx1: TCtx, ctx2: TCtx) -> TCtx
