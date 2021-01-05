package com.github.trilobiitti.trilobite.dna.operations

import com.github.trilobiitti.trilobite.dna.*
import com.github.trilobiitti.trilobite.dna.di.Registry
import com.github.trilobiitti.trilobite.dna.di.UnregisteredRegistryItemException

class DefaultOperation<TCtx : Any, TStage>(
    private val plan: OperationPlan<TCtx, TStage>,
    private val executor: OperationPlanExecutor<TCtx, TStage>
) : Operation<TCtx> {
    override suspend fun execute(input: TCtx): TCtx = executor.execute(plan, input)
}

class DefaultOperationEditor<TCtx : Any, TStage : Any>(
    private val registry: OperationRegistry<TCtx, TStage>,
    private val name: Any,
    private val deciderBuilder: DeciderBuilder<TCtx, CasePlanPatch<TStage>>
) : OperationPlanEditor<TCtx, TStage> {
    private val decisionFactory = ContextIndependentImmutableDecisionFactory<
        TCtx,
        CasePlanPatch<TStage>,
        Order<*, TStage>
        > { casePlanPatches ->
        DefaultOrder<TStage>().also { for (cpp in casePlanPatches) cpp(it) }
    }

    private fun buildOperation(): Operation<TCtx> = DefaultOperation(
        deciderBuilder.build(decisionFactory),
        registry.executor()
    )

    override fun applyConditionalPatch(
        condition: Iterable<DecisionCondition<TCtx, *>>,
        planPatch: CasePlanPatch<TStage>
    ) {
        deciderBuilder.addRule(condition, planPatch)
    }

    override fun flush() {
        val op by lazy { buildOperation() }
        registry.register(name) { op }
    }
}

class DefaultOperationManager<TCtx : Any, TStage : Any>(
    private val registry: OperationRegistry<TCtx, TStage>
) {
    private val editorsRegistry = Registry.new<OperationPlanEditor<TCtx, TStage>>(registry.edit.key)

    fun install() {
        registry.edit.use { operationName, patch ->
            val editor = try {
                editorsRegistry.resolve(operationName)
            } catch (e: UnregisteredRegistryItemException) {
                val editor = DefaultOperationEditor(registry, operationName, DefaultDeciderBuilder())

                // TODO: Cannot be sure that another instance was not registered concurrently...
                editorsRegistry.register(operationName) { editor }

                // Ensure that at least an empty operation is present in the registry.
                editor.flush()

                editor
            }

            patch(editor)
        }
    }

    companion object {
        fun <TCtx : Any, TStage : Any> install(registry: OperationRegistry<TCtx, TStage>) {
            DefaultOperationManager(registry).install()
        }
    }
}
