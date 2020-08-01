package com.github.trilobiitti.trilobite.dna

typealias DeciderInputBase = Any
typealias DeciderVariableValueBase = Any
typealias DeciderItemBase = Any

/**
 * DecisionVariable is *suddenly* a function of [Decider] input and (probably some other [DecisionVariable]'s).
 *
 * [Decider] will compute values of [DecisionVariable]'s for passed input object when they are necessary to take the
 * decision.
 *
 * [DecisionVariable] in most cases will be a pure function of decider's input (and other variables, but recursively
 * still of input only). But it isn't disallowed for it do depend on some external conditions e.g. a certain variable
 * depending on current date may change it's value in friday's evening.
 *
 * If accessed by other variables through [DecisionContext] only, the variable will be computed at most once for any
 * call of [Decider].
 *
 * @param TIn type of decider input expected by this variable
 * @param TVar type of this variable's value
 */
interface DecisionVariable<in TIn : DeciderInputBase, out TVar : DeciderVariableValueBase> {
    /**
     * Computes a value of the variable using input and values of other variables from [context].
     *
     * MUST NOT be called within production code from anywhere except [DecisionContext] implementation. Use
     * [DecisionContext.get] to get value of variable for current decision.
     */
    fun getFrom(context: DecisionContext<TIn>): TVar

    /**
     * Returns list of all variables this one may read from [DecisionContext] when is being computed.
     *
     * NOTE: Access to any variables except the ones listed in this list from [getFrom] will lead to
     * undefined behavior.
     */
    fun getDependencies(): List<DecisionVariable<TIn, *>> = emptyList()

    /**
     * Must return `true` iff this variable doesn't interact with [DecisionContext.input] directly.
     *
     * NOTE: Access to decider input from [getFrom] method of a variable returning `true` from [isMetaVariable]
     * will lead to undefined behavior.
     */
    fun isMetaVariable(): Boolean = false
}

/**
 * A object [DecisionVariable] implementations may access decider input and values of other variables through.
 *
 * @param TIn type of decider input
 */
interface DecisionContext<out TIn : DeciderInputBase> {
    /**
     * Decider input for current decision.
     */
    val input: TIn

    /**
     * Returns value of [variable] for current decision.
     */
    operator fun <TVar : DeciderVariableValueBase> get(variable: DecisionVariable<TIn, TVar>): TVar
}

/**
 * Represents a single constraint on a [DecisionVariable].
 *
 * It could be represented by a [Pair] or [Map.Entry] but it was added as a separate class to provide a slightly better
 * typing. It is just *slightly* better because it's always possible to cast a [DecisionVariable] to
 * `DecisionVariable<TIn, DeciderVariableValueBase>` and provide any value of type [DeciderVariableValueBase] (which
 * currently is [Any] and is added itself just for some verbosity).
 *
 * @param TIn type of decider input
 * @param TVar value type of the [variable]
 */
class DecisionCondition<TIn : DeciderInputBase, TVar : DeciderVariableValueBase>(
        /**
         * Variable that is being constrained.
         */
        val variable: DecisionVariable<TIn, TVar>,

        /**
         * Value this variable should have for this constraint to be satisfied.
         */
        val value: TVar
)

/**
 * Builder for [Decider].
 *
 * The builder is not expected to be thread-safe.
 *
 * @param TIn type of decider input
 * @param TItem type of items decider output is constructed from
 */
interface DeciderBuilder<TIn : DeciderInputBase, TItem : DeciderItemBase> {
    /**
     * Adds a rule to the decider.
     *
     * TODO: It may be a better strategy to just ignore the rules with contradicting conditions
     * @throws IllegalArgumentException when some conditions explicitly contradict to each other i.e require the same
     *                                  variable to have different values.
     */
    fun addRule(conditions: Iterable<DecisionCondition<TIn, *>>, item: TItem)

    /**
     * Creates a [Decider] instance based on previously provided (by calls of [addRule]) rules.
     *
     * The created decider will use [outFactory] to create outputs (of type [TOut]) from sets of items (of type
     * [TItem]). [DeciderBuilder]/[Decider] implementation may call [outFactory] either immediately on [build] call
     * (for all possible decisions) or lazily (on decider invocations). So [outFactory] must be thread-safe if the
     * resulting [Decider] will be used concurrently and [DeciderBuilder] is not chosen/configured the way that
     * [outFactory] is invoked immediately.
     */
    fun <TOut> build(outFactory: (Set<TItem>) -> TOut): Decider<TIn, TOut>
}

/**
 * Decider is a function that takes an input value (of type [TIn]]) and constructs an output value (of type [TOut])
 * from set of items chosen using preconfigured rules.
 *
 * Each rule is represented by set of constraints on variables (see [DecisionVariable]) computable from input value
 * and an item that should be included in the set if all those constraints are satisfied (see [DeciderBuilder.addRule]).
 *
 * You may think of it as of a dynamic `switch` operator that takes multiple input variables and produces multiple
 * results.
 */
interface Decider<TIn : DeciderInputBase, TOut> : (TIn) -> TOut
