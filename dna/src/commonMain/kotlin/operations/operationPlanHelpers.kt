package com.github.trilobiitti.trilobite.dna.operations

import com.github.trilobiitti.trilobite.dna.DecisionCondition
import com.github.trilobiitti.trilobite.dna.SequenceBuilder
import com.github.trilobiitti.trilobite.dna.SequentialSequenceBuilder

/**
 * Adds conditional sequence of stages.
 *
 * Unlike [OperationPlanEditor.applyConditionalPatch] provides an easier-to-use [SequenceBuilder] facade instead of a
 * raw [OrderBuilder][com.github.trilobiitti.trilobite.dna.OrderBuilder].
 */
inline fun <TCtx : Any, TStage : Any> OperationPlanEditor<TCtx, TStage>.add(
    condition: Iterable<DecisionCondition<TCtx, *>>,
    crossinline block: SequenceBuilder<*, TStage>.() -> Unit
) {
    applyConditionalPatch(condition) { SequentialSequenceBuilder(this).run(block) }
}

/**
 * Adds unconditional sequence of stages.
 *
 * Like [add] but accepts no conditions.
 */
inline fun <TCtx : Any, TStage : Any> OperationPlanEditor<TCtx, TStage>.alwaysAdd(
    crossinline block: SequenceBuilder<*, TStage>.() -> Unit
) = add(emptyList(), block)
