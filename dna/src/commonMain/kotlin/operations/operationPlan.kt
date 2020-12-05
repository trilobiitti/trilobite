package com.github.trilobiitti.trilobite.dna.operations

import com.github.trilobiitti.trilobite.dna.Decider
import com.github.trilobiitti.trilobite.dna.DecisionCondition
import com.github.trilobiitti.trilobite.dna.Order
import com.github.trilobiitti.trilobite.dna.OrderBuilder

typealias CasePlanPatch<TStage> = OrderBuilder<*, TStage>.() -> Unit

interface OperationPlanEditor<TCtx : Any, TStage> {
    fun add(
        condition: Iterable<DecisionCondition<TCtx, *>>,
        planPatch: CasePlanPatch<TStage>
    )
}

typealias OperationPlan<TCtx, TStage> = Decider<TCtx, Order<*, TStage>>

interface OperationPlanExecutor<TCtx : Any, TStage> {
    suspend fun executeCase(
        stages: Order<*, TStage>,
        context: TCtx
    ): TCtx

    suspend fun execute(
        plan: OperationPlan<TCtx, TStage>,
        context: TCtx
    ): TCtx = executeCase(
        plan.invoke(context),
        context
    )
}

typealias StageExecutor<TStage, TCtx> = suspend (stage: TStage, context: TCtx) -> TCtx

typealias ContextReducer<TCtx> = (ctx1: TCtx, ctx2: TCtx) -> TCtx
