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
    conditions.add(DecisionCondition(
            PredicateVariable(variable, predicate),
            !negate
    ))
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

data class PredicateVariable<TIn: DeciderInputBase, TVar: DeciderVariableValueBase>(
        val variable: DecisionVariable<TIn, TVar>,
        val predicate: (TVar) -> Boolean
): DecisionVariable<TIn, Boolean> {
    override fun getFrom(context: DecisionContext<TIn>): Boolean = predicate(context[variable])

    override fun getDependencies(): List<DecisionVariable<TIn, *>> = listOf(variable)

    override fun isMetaVariable(): Boolean = true
}
