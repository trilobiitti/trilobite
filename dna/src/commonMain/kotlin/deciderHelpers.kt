package com.github.trilobiitti.trilobite.dna

//
// DSLs
//

@DslMarker
annotation class DeciderConditionsDsl

@DeciderConditionsDsl
class DeciderConditionsBuilder<TIn : DeciderInputBase> {
    val conditions: MutableList<DecisionCondition<TIn, *>> = mutableListOf()
}

fun <TIn : DeciderInputBase, TVar : DeciderVariableValueBase>
DeciderConditionsBuilder<TIn>.expect(
    variable: DecisionVariable<TIn, TVar>,
    value: TVar
) {
    conditions.add(DecisionCondition(variable, value))
}

fun <TIn : DeciderInputBase, TVar : DeciderVariableValueBase>
DeciderConditionsBuilder<TIn>.expect(
    variable: DecisionVariable<TIn, TVar>,
    negate: Boolean = false,
    predicate: (TVar) -> Boolean
) {
    conditions.add(
        DecisionCondition(
            PredicateVariable(variable, predicate),
            !negate
        )
    )
}

fun <TIn : DeciderInputBase, TItem : DeciderItemBase>
DeciderBuilder<TIn, TItem>.add(item: TItem, block: DeciderConditionsBuilder<TIn>.() -> Unit) {
    addRule(
        DeciderConditionsBuilder<TIn>().also(block).conditions,
        item
    )
}

//
// Commonly useful classes
//

data class PredicateVariable<TIn : DeciderInputBase, TVar : DeciderVariableValueBase>(
    val variable: DecisionVariable<TIn, TVar>,
    val predicate: (TVar) -> Boolean
) : DecisionVariable<TIn, Boolean> {
    override fun getFrom(context: DecisionContext<TIn>): Boolean = predicate(context[variable])

    override fun getDependencies(): List<DecisionVariable<TIn, *>> = listOf(variable)

    override fun isMetaVariable(): Boolean = true
}

data class RegexpMatchVariable<TIn : DeciderInputBase>(
    private val variable: DecisionVariable<TIn, String>,
    private val pattern: String
) : DecisionVariable<TIn, Boolean> {
    // Regex doesn't support equality check (Regex(".*") != Regex(".*")) so only regex's pattern is kept
    // as constructor parameter (and thus is used for generated hash/comparison methods)
    private val regex = Regex(pattern)

    override fun getFrom(context: DecisionContext<TIn>): Boolean = context[variable].matches(regex)

    override fun getDependencies(): List<DecisionVariable<TIn, *>> = listOf(variable)

    override fun isMetaVariable(): Boolean = true

    override fun toString(): String = "$variable matches Regex($pattern)"
}

data class CollectionSizeVariable<TIn : DeciderInputBase>(
    private val variable: DecisionVariable<TIn, Collection<*>>
) : DecisionVariable<TIn, Int> {
    override fun getFrom(context: DecisionContext<TIn>): Int = context[variable].size
    override fun getDependencies(): List<DecisionVariable<TIn, *>> = listOf(variable)
    override fun isMetaVariable(): Boolean = true
    override fun toString(): String = "size of $variable"
}

class IdentityVariable<TIn : DeciderInputBase> /*where TIn: DeciderVariableValueBase*/ : DecisionVariable<TIn, TIn> {
    override fun getFrom(context: DecisionContext<TIn>): TIn = context.input

    override fun equals(other: Any?): Boolean = other is IdentityVariable<*>
    override fun hashCode(): Int = 666
}

class ContextIndependentImmutableDecisionFactory<TIn : DeciderInputBase, TItem : DeciderItemBase, TOut>(
    private val transform: (items: Iterable<TItem>) -> TOut
) : DecisionFactory<TIn, TItem, TOut, TOut> {
    override fun initDecisionInvariant(items: Iterable<TItem>): TOut = transform(items)

    override fun generateOutput(decisionInvariant: TOut, context: DecisionContext<TIn>): TOut = decisionInvariant
}

class FullyContextDependentDecisionFactory<TIn : DeciderInputBase, TItem : DeciderItemBase, TOut>(
    private val create: (items: Iterable<TItem>, context: DecisionContext<TIn>) -> TOut
) : DecisionFactory<TIn, TItem, Iterable<TItem>, TOut> {
    override fun initDecisionInvariant(items: Iterable<TItem>): Iterable<TItem> = items

    override fun generateOutput(decisionInvariant: Iterable<TItem>, context: DecisionContext<TIn>): TOut =
        create(decisionInvariant, context)
}
