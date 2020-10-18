package com.github.trilobiitti.trilobite.dna

// WTF-KOTLIN-JS-OF-THE-DAY: Initially this was parametrized as DecisionVariable<DeciderInputBase, Unit> but (probably)
// returned `Unit` value causes cast error in [DefaultDecisionContext.get] so, as output returned from this variable is
// not used anywhere anyway, let it just return itself
object NullDecisionVariable : DecisionVariable<DeciderInputBase, NullDecisionVariable> {
    override fun getFrom(context: DecisionContext<DeciderInputBase>) = NullDecisionVariable
}

private class DecisionTreeNode<TIn : DeciderInputBase, TItem : DeciderItemBase, TInv, TOut>(
    val variable: DecisionVariable<TIn, DeciderVariableValueBase>,
    val items: Iterable<TItem>,
    val children: Map<DeciderVariableValueBase, DecisionTreeNode<TIn, TItem, *, TOut>>?,
    val defaultChild: DecisionTreeNode<TIn, TItem, *, TOut>?,
    val outputFactory: DecisionFactory<TIn, TItem, TInv, TOut>
) {
    init {
        children?.forEach { (_, it) -> it.parent = this }
        defaultChild?.parent = this
    }

    var parent: DecisionTreeNode<TIn, TItem, *, TOut>? = null
    val decisionInvariant: TInv by lazy {
        outputFactory.initDecisionInvariant(
            mutableListOf<TItem>().also { addItemsTo(it) }
        )
    }

    /**
     * Add items attached to this node and it's parents to given set.
     */
    fun addItemsTo(collection: MutableCollection<TItem>) {
        collection.addAll(items)
        parent?.addItemsTo(collection)
    }

    fun print(dst: Appendable, offset: String) {
        dst.append("$offset* by $variable\n")
        items.forEach { dst.append("$offset- $it\n") }
        children?.forEach { (k, v) ->
            dst.append("$offset+ when $k\n")
            v.print(dst, "$offset\t")
        }
        defaultChild?.let {
            dst.append("$offset+ else\n")
            it.print(dst, "$offset\t")
        }
    }

    companion object {
        tailrec fun <TIn : DeciderInputBase, TItem : DeciderItemBase, TInv, TOut> process(
            node: DecisionTreeNode<TIn, TItem, TInv, TOut>,
            context: DecisionContext<TIn>
        ): TOut {
            val value = context[node.variable]

            val chosenChild = node.children?.get(value) ?: node.defaultChild

            if (chosenChild !== null) {
                return process(chosenChild, context)
            }

            return node.outputFactory.generateOutput(node.decisionInvariant, context)
        }
    }
}

class DefaultDecisionContext<TIn : DeciderInputBase>(
    override val input: TIn
) : DecisionContext<TIn> {
    private val resolved: MutableMap<DecisionVariable<TIn, *>, DeciderVariableValueBase> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <TVar : DeciderVariableValueBase> get(variable: DecisionVariable<TIn, TVar>): TVar =
        resolved.getOrPut(variable) { variable.getFrom(this) } as TVar
}

private class TreeDecider<TIn : DeciderInputBase, TItem : DeciderItemBase, TInv, TOut>(
    private val root: DecisionTreeNode<TIn, TItem, TInv, TOut>
) : Decider<TIn, TOut> {
    override fun invoke(input: TIn): TOut {
        val context = DefaultDecisionContext(input)
        return DecisionTreeNode.process(root, context)
    }

    override fun toString(): String = StringBuilder()
        .append("${super.toString()}\n")
        .also { root.print(it, "") }.toString()
}

private val DEFAULT_SENTINEL: Any = object {}

private class AssumedDecisionContext<TIn : DeciderInputBase>(
    val targetVariable: DecisionVariable<TIn, *>,
    val assumptions: Map<DecisionVariable<TIn, *>, DeciderVariableValueBase>
) : DecisionContext<TIn> {
    override val input: TIn
        get() = throw IllegalStateException("Unexpected input dependency for variable $targetVariable")

    @Suppress("UNCHECKED_CAST")
    override fun <TVar : DeciderVariableValueBase> get(variable: DecisionVariable<TIn, TVar>): TVar =
        (
            assumptions[variable]
                ?: throw IllegalStateException("Unexpected dependency on $variable for $targetVariable")
            )
            as TVar
}

class DefaultDeciderBuilder<TIn : DeciderInputBase, TItem : DeciderItemBase> : DeciderBuilder<TIn, TItem> {
    private class Rule<TIn : DeciderItemBase, TItem : DeciderItemBase>(
        val conditions: MutableMap<DecisionVariable<TIn, DeciderVariableValueBase>, DeciderVariableValueBase>,
        val item: TItem
    ) {
        fun copy() = Rule(conditions.toMutableMap(), item)
    }

    private val rules: MutableList<Rule<TIn, TItem>> = mutableListOf()

    override fun addRule(conditions: Iterable<DecisionCondition<TIn, *>>, item: TItem) {
        val conditionsMap = mutableMapOf(*(conditions.map { it.variable to it.value }).toTypedArray())

        if (conditionsMap.size < conditions.count()) {
            val contradictingConditions = conditions
                .groupBy({ it.variable }, { it.value })
                .mapValues { it.value.toSet() }
                .filter { it.value.size > 1 }

            if (contradictingConditions.isNotEmpty()) {
                throw IllegalArgumentException(
                    "Contradicting conditions are provided for ${
                    if (contradictingConditions.size > 1) "some variables" else "a variable"
                    }: ${
                    contradictingConditions
                        .map { rule ->
                            "variable ${rule.key} is expected to be both ${
                            rule.value.joinToString(" and ") { "\"$it\"" }}"
                        }
                        .joinToString("; ")
                    }"
                )
            }
        }

        rules.add(
            Rule(conditionsMap, item)
        )
    }

    private fun pickSplitVariable(rules: Iterable<Rule<TIn, TItem>>): DecisionVariable<TIn, DeciderVariableValueBase>? {
        val counts: MutableMap<DecisionVariable<TIn, DeciderVariableValueBase>, Int> = mutableMapOf()

        rules.forEach { rule ->
            rule.conditions.keys.forEach { variable ->
                counts[variable] = counts.getOrElse(variable, { 0 }) + 1
            }
        }

        rules.forEach { rule ->
            rule.conditions.keys.forEach { variable ->
                variable.getDependencies().forEach { dependency ->
                    val c = counts[dependency]
                    if (c !== null) {
                        counts[dependency] = c + 1
                    }
                }
            }
        }

        return counts.entries.maxByOrNull { it.value }?.key
    }

    private fun <TOut, TInv> buildTree(
        rules_: Iterable<Rule<TIn, TItem>>,
        outFactory: DecisionFactory<TIn, TItem, TInv, TOut>,
        assumptions_: Map<DecisionVariable<TIn, DeciderVariableValueBase>, DeciderVariableValueBase>
    ): DecisionTreeNode<TIn, TItem, TInv, TOut> {
        var assumptions = assumptions_
        var rules: Iterable<Rule<TIn, TItem>> = rules_
        var splitVar: DecisionVariable<TIn, DeciderVariableValueBase>?
        var grouped: MutableMap<DeciderVariableValueBase, MutableList<Rule<TIn, TItem>>> = mutableMapOf()

        while (true) {
            splitVar = pickSplitVariable(rules)

            if (splitVar === null) {
                // If there are no more variables to choose from then remaining rules have no more additional conditions
                // and the new node will become a leaf
                return DecisionTreeNode(
                    NullDecisionVariable,
                    rules.map { it.item },
                    null,
                    null,
                    outFactory
                )
            }

            // Group the rules to a mutable map where key is value of split variable expected by the rule or
            // DEFAULT_SENTINEL (which marks a group of rules that do not involve the variable)
            rules.groupByTo(grouped) { it.conditions[splitVar] ?: DEFAULT_SENTINEL }

            // If `splitVar` is normal variable then we must use it as is
            if (!splitVar.isMetaVariable()) break

            val dependencies = splitVar.getDependencies()

            // If `splitVar` is a meta variable without dependencies (probably depending on some conditions
            // not related to input) then we should handle it as a normal variable.
            if (dependencies.isEmpty()) break

            // If `splitVar` is meta variable but values of some it's dependencies are unknown then we also have
            // to use it as is
            if (!assumptions.keys.containsAll(dependencies)) break

            // ...but otherwise we know the exact value of the variable at this moment
            val knownValue = splitVar.getFrom(AssumedDecisionContext(splitVar, assumptions))

            // ...so we can leave only the rules that expect exactly that value and the rules that don't use
            // the variable. Others will not match anyway.
            val remainingRules = mutableListOf<Rule<TIn, TItem>>()
            grouped[knownValue]?.apply { forEach { it.conditions.remove(splitVar) } }?.let { remainingRules.addAll(it) }
            grouped[DEFAULT_SENTINEL]?.let { remainingRules.addAll(it) }

            rules = remainingRules
            grouped = mutableMapOf()
            assumptions = assumptions + mapOf(splitVar to knownValue)
        }

        splitVar!!

        // Get set of rules that do not involve the chosen split variable and remove list of them from the map,
        // so map keys now include possible values of the split variable only
        val defaultRules = grouped[DEFAULT_SENTINEL]
        grouped.remove(DEFAULT_SENTINEL)

        // Delete variable chosen for new node from all rule sets for children nodes so it will not
        // be directly used by children of new node
        grouped.values.forEach { rulesGroup -> rulesGroup.forEach { rule -> rule.conditions.remove(splitVar) } }

        var nodeItems: Iterable<TItem> = emptyList()
        var defaultChild: DecisionTreeNode<TIn, TItem, TInv, TOut>? = null

        if (defaultRules !== null) {
            // Add rules that do not involve the chosen variable but still have some conditions to all children nodes
            val nonEmptyRules = defaultRules.filter { it.conditions.isNotEmpty() }
            grouped.values.forEach { rulesGroup ->
                rulesGroup.addAll(nonEmptyRules.map { it.copy() })
            }

            // Fill list of items assigned to this node unconditionally
            nodeItems = defaultRules.filter { it.conditions.isEmpty() }.map { it.item }

            // Build default child for new node if necessary
            if (nonEmptyRules.isNotEmpty()) {
                defaultChild = buildTree(nonEmptyRules, outFactory, assumptions)
            }
        }

        // Build child nodes
        val children = grouped.mapValues { (value, it) ->
            buildTree(it, outFactory, assumptions + mapOf(splitVar to value))
        }

        // Finally, create the node
        return DecisionTreeNode(
            splitVar,
            nodeItems,
            children,
            defaultChild,
            outFactory
        )
    }

    override fun <TInv, TOut> build(decisionFactory: DecisionFactory<TIn, TItem, TInv, TOut>): Decider<TIn, TOut> =
        TreeDecider(
            buildTree(
                // Copy rules list so this builder can be reused later with (or without) new rules added
                rules.map { it.copy() },
                decisionFactory,
                mapOf()
            )
        )
}
