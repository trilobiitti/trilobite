package com.github.trilobiitti.trilobite.dna

private class NullDecisionVariable<TIn : DeciderInputBase> : DecisionVariable<TIn, Unit> {
    override fun getFrom(context: DecisionContext<TIn>): Unit = Unit
}

private class DecisionTreeNode<TIn : DeciderInputBase, TVar : DeciderVariableValueBase, TItem : DeciderItemBase, TOut>(
        val variable: DecisionVariable<TIn, TVar>,
        val items: Iterable<TItem>,
        val children: Map<TVar, DecisionTreeNode<TIn, *, TItem, TOut>>?,
        val defaultChild: DecisionTreeNode<TIn, *, TItem, TOut>?,
        val outputFactory: (Set<TItem>) -> TOut
) {
    init {
        children?.forEach { (_, it) -> it.parent = this }
        defaultChild?.parent = this
    }

    var parent: DecisionTreeNode<TIn, *, TItem, TOut>? = null
    val output: TOut by lazy {
        outputFactory(mutableSetOf<TItem>().also { addItemsTo(it) })
    }

    /**
     * Add items attached to this node and it's parents to given set.
     */
    fun addItemsTo(set: MutableSet<TItem>) {
        set.addAll(items)
        parent?.addItemsTo(set)
    }

    companion object {
        tailrec fun <TIn : DeciderInputBase, TVar : DeciderVariableValueBase, TItem : DeciderItemBase, TOut> process(
                node: DecisionTreeNode<TIn, TVar, TItem, TOut>,
                context: DecisionContext<TIn>
        ): TOut {
            val value = context[node.variable]

            val chosenChild = node.children?.get(value) ?: node.defaultChild

            if (chosenChild !== null) {
                return process(chosenChild, context)
            }

            return node.output
        }
    }
}

private class DefaultDecisionContext<TIn : DeciderInputBase>(
        override val input: TIn
) : DecisionContext<TIn> {
    private val resolved: MutableMap<DecisionVariable<TIn, *>, DeciderVariableValueBase> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <TVar : DeciderVariableValueBase> get(variable: DecisionVariable<TIn, TVar>): TVar =
            resolved.getOrPut(variable) { variable.getFrom(this) } as TVar
}

private class TreeDecider<TIn : DeciderInputBase, TItem : DeciderItemBase, TOut>(
        private val root: DecisionTreeNode<TIn, *, TItem, TOut>
) : Decider<TIn, TOut> {
    override fun invoke(input: TIn): TOut {
        val context = DefaultDecisionContext(input)
        return DecisionTreeNode.process(root, context)
    }
}

private val DEFAULT_SENTINEL = object {}

class DefaultDeciderBuilder<TIn : DeciderInputBase, TItem : DeciderItemBase> : DeciderBuilder<TIn, TItem> {
    private class Rule<TIn : DeciderItemBase, TItem : DeciderItemBase>(
            val conditions: MutableMap<DecisionVariable<TIn, DeciderVariableValueBase>, DeciderVariableValueBase>,
            val item: TItem
    ) {
        fun copy() = Rule(conditions.toMutableMap(), item)
    }

    private val rules: MutableList<Rule<TIn, TItem>> = mutableListOf()

    override fun addRule(conditions: Iterable<DecisionCondition<TIn, *>>, item: TItem) {
        rules.add(
                Rule(mutableMapOf(*(conditions.map { it.variable to it.value }).toTypedArray()),
                        item)
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

        return counts.entries.maxBy { it.value }?.key
    }

    private fun <TOut> buildTree(
            rules: Iterable<Rule<TIn, TItem>>,
            outFactory: (Set<TItem>) -> TOut
    ): DecisionTreeNode<TIn, *, TItem, TOut> {
        val splitVar = pickSplitVariable(rules)

        if (splitVar === null) {
            // If there are no more variables to choose from then remaining rules have no more additional conditions
            // and the new node will become a leaf
            return DecisionTreeNode(
                    NullDecisionVariable(),
                    rules.map { it.item },
                    null,
                    null,
                    outFactory
            )
        }

        // Group the rules to a mutable map where key is value of split variable expected by the rule or
        // DEFAULT_SENTINEL (which marks a group of rules that do not involve the variable)
        val grouped: MutableMap<DeciderVariableValueBase, MutableList<Rule<TIn, TItem>>> = mutableMapOf()
        rules.groupByTo(grouped) { it.conditions[splitVar] ?: DEFAULT_SENTINEL }

        // Get set of rules that do not involve the chosen split variable and remove list of them from the map,
        // so map keys now imclude possible values of the split variable only
        val defaultRules = grouped[DEFAULT_SENTINEL]
        grouped.remove(DEFAULT_SENTINEL)

        // Delete variable chosen for new node from all rule sets for children nodes so it will not
        // be directly used by children of new node
        grouped.values.forEach { rulesGroup -> rulesGroup.forEach { rule -> rule.conditions.remove(splitVar) } }

        var nodeItems: Iterable<TItem> = emptyList()
        var defaultChild: DecisionTreeNode<TIn, *, TItem, TOut>? = null

        if (defaultRules !== null) {
            // Add rules that do not involve the chosen variable but still have some conditions to all children nodes
            val nonEmptyRules = defaultRules.filter { it.conditions.isNotEmpty() }
            grouped.values.forEach { rulesGroup ->
                rulesGroup.addAll(nonEmptyRules.map { it.copy() })
            }

            // Fill list of items assigned to this node unconditionally
            nodeItems = defaultRules.filter { it.conditions.isEmpty() }.map { it.item }

            // Build default child for new node
            defaultChild = buildTree(defaultRules, outFactory)
        }

        // Build child nodes
        val children = grouped.mapValues { (_, it) -> buildTree(it, outFactory) }

        // Finally, create the node
        return DecisionTreeNode(
                splitVar,
                nodeItems,
                children,
                defaultChild,
                outFactory
        )
    }

    override fun <TOut> build(outFactory: (Set<TItem>) -> TOut): Decider<TIn, TOut> =
            TreeDecider(
                    buildTree(
                            // Copy rules list so this builder can be reused later with (or without) new rules added
                            rules.map { it.copy() },
                            outFactory
                    )
            )
}
