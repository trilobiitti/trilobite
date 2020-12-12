package com.github.trilobiitti.trilobite.dna.operations

import com.github.trilobiitti.trilobite.dna.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlin.coroutines.CoroutineContext

class SequentialOperationPlanExecutor<TCtx : Any, TStage>(
    private val stageExecutor: StageExecutor<TStage, TCtx>
) : OperationPlanExecutor<TCtx, TStage> {
    override suspend fun executeCase(stages: Order<*, TStage>, context: TCtx): TCtx {
        var currentContext = context

        for (stage in stages.toLinearList()) {
            currentContext = stageExecutor(stage, context)
        }

        return currentContext
    }
}

class SynchronousSequentialOperationPlanExecutor<TCtx : Any, TStage>(
    private val stageExecutor: SynchronousStageExecutor<TStage, TCtx>
) : OperationPlanExecutor<TCtx, TStage> {
    override suspend fun executeCase(stages: Order<*, TStage>, context: TCtx): TCtx {
        var currentContext = context

        for (stage in stages.toLinearList()) {
            currentContext = stageExecutor(stage, context)
        }

        return currentContext
    }
}

class ConcurrentOperationPlanExecutor<TCtx : Any, TStage>(
    private val stageExecutor: StageExecutor<TStage, TCtx>,
    private val contextReducer: ContextReducer<TCtx>,
    private val coroutineContext: CoroutineContext
) : OperationPlanExecutor<TCtx, TStage> {
    private suspend fun awaitAndReduce(prevResults: Iterable<Deferred<TCtx>>, initialContext: TCtx): TCtx {
        var currentContext = initialContext

        for (deferred in prevResults) {
            currentContext = contextReducer(currentContext, deferred.await())
        }

        return currentContext
    }

    override suspend fun executeCase(stages: Order<*, TStage>, context: TCtx): TCtx {
        val scope = CoroutineScope(coroutineContext)

        return awaitAndReduce(
            stages.visit { _, items, prevResults: Iterable<Deferred<TCtx>> ->
                return@visit scope.async {
                    val stageCtx = awaitAndReduce(prevResults, context)

                    return@async awaitAndReduce(
                        items.map { item -> async { stageExecutor(item, stageCtx) } },
                        stageCtx
                    )
                }
            },
            context
        )
    }
}
