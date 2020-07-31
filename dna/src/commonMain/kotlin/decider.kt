package com.github.trilobiitti.trilobite.dna

typealias DeciderInputBase = Any
typealias DeciderVariableValueBase = Any
typealias DeciderItemBase = Any

interface DecisionVariable<in TIn: DeciderInputBase, out TVar: DeciderVariableValueBase> {
    fun getFrom(context: DecisionContext<TIn>): TVar

    fun getDependencies(): List<DecisionVariable<TIn, *>> = emptyList()

    /**
     * Must return `true` iff this variable doesn't interact with [DecisionContext.input] directly.
     */
    fun isMetaVariable(): Boolean = false
}

interface DecisionContext<out TIn: DeciderInputBase> {
    val input: TIn

    operator fun <TVar: DeciderVariableValueBase> get(variable: DecisionVariable<TIn, TVar>): TVar
}

class DecisionCondition<TIn: DeciderInputBase, TVar: DeciderVariableValueBase>(
        val variable: DecisionVariable<TIn, TVar>,
        val value: TVar
)

/**
 * Builder for [Decider].
 */
interface DeciderBuilder<TIn: DeciderInputBase, TItem: DeciderItemBase> {
    fun addRule(conditions: Iterable<DecisionCondition<TIn, *>>, item: TItem)

    fun <TOut> build(outFactory: (Set<TItem>) -> TOut): Decider<TIn, TOut>
}

/**
 * Decider is a function that takes an input value (of type [TIn]]) and constructs an output value (of type [TOut])
 * from set of items chosen using preconfigured rules.
 *
 * Each rule is represented by set of constraints on variables computable from input value and an item that should be
 * included in the set if all those constraints are satisfied (see [DeciderBuilder.addRule]).
 */
interface Decider<TIn: DeciderInputBase, TOut> : (TIn) -> TOut
