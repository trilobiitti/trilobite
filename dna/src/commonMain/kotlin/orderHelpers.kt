package com.github.trilobiitti.trilobite.dna

operator fun <TKey, TValue> OrderBuilder<TKey, TValue>.set(key: Any, value: TValue) {
    register(parseKey(key), value)
}

operator fun <TKey, TValue> OrderBuilder<TKey, TValue>.plusAssign(p: Pair<Any, Any>) {
    link(parseKey(p.first), parseKey(p.second))
}

@DslMarker
annotation class SequenceDsl

/**
 * SequenceBuilder is a facade for [OrderBuilder] provides a DSL for (sub)sequences of items.
 *
 * @see [seq]
 * @see [par]
 * @see [add]
 * @see [expect]
 */
@SequenceDsl
interface SequenceBuilder<TKey, TValue> {
    /**
     * The [OrderBuilder] this builder is facade for.
     */
    val orderBuilder: OrderBuilder<TKey, TValue>

    /**
     * @return list of dependencies of current item
     */
    fun getCurrentDependencies(): Iterable<TKey>

    /**
     * Should be called after item or set of items is added to [orderBuilder].
     *
     * @param deps keys of added item(s)
     */
    fun putDependencies(deps: Iterable<TKey>)
}

class SequentialSequenceBuilder<TKey, TValue>(
    override val orderBuilder: OrderBuilder<TKey, TValue>,
    prevDeps: Iterable<TKey>
) : SequenceBuilder<TKey, TValue> {
    private var curDeps = prevDeps

    override fun getCurrentDependencies(): Iterable<TKey> = curDeps

    override fun putDependencies(deps: Iterable<TKey>) {
        curDeps = deps
    }
}

/**
 * Describes a set of items that should be ordered in the same order as they are placed inside the block.
 *
 * @see [SequenceBuilder]
 */
inline fun <TKey, TValue> SequenceBuilder<TKey, TValue>.seq(block: SequenceBuilder<TKey, TValue>.() -> Unit) {
    if (this is SequentialSequenceBuilder) {
        return block(this)
    }

    putDependencies(
        SequentialSequenceBuilder(orderBuilder, getCurrentDependencies())
            .also(block)
            .getCurrentDependencies()
    )
}

class ParallelSequenceBuilder<TKey, TValue>(
    override val orderBuilder: OrderBuilder<TKey, TValue>,
    private val prevDeps: Iterable<TKey>
) : SequenceBuilder<TKey, TValue> {
    private val nextDeps = mutableSetOf<TKey>()

    override fun getCurrentDependencies(): Iterable<TKey> = prevDeps

    override fun putDependencies(deps: Iterable<TKey>) {
        nextDeps.addAll(deps)
    }

    fun getNextDeps(): Iterable<TKey> = if (nextDeps.isEmpty()) {
        prevDeps
    } else {
        nextDeps
    }
}

/**
 * Describes a set of items that are not dependent on each other but may depend on items listed before `par` block when
 * it is placed inside [seq] block.
 * When placed inside [seq] block, the item(s) after the `par` block will depend on all of items listed inside `par`
 * block.
 *
 * @see [SequenceBuilder]
 */
inline fun <TKey, TValue> SequenceBuilder<TKey, TValue>.par(block: SequenceBuilder<TKey, TValue>.() -> Unit) {
    if (this is ParallelSequenceBuilder) {
        return block(this)
    }

    putDependencies(
        ParallelSequenceBuilder(orderBuilder, getCurrentDependencies())
            .also(block)
            .getNextDeps()
    )
}

/**
 * Same as [expect] but accepts a parsed key.
 *
 * @see [expect]
 * @see SequenceBuilder
 */
fun <TKey, TValue> SequenceBuilder<TKey, TValue>.expectKey(key: TKey) {
    for (dep in getCurrentDependencies()) orderBuilder.link(dep, key)
    putDependencies(listOf(key))
}

/**
 * Declares a key of the item without actually defining the item.
 * Can be used to declare dependency on other item that is defined elsewhere.
 * Item keys without attached items can exist in sequences and serve as barriers.
 *
 * @param key key of the item
 *
 * @see [SequenceBuilder]
 */
fun <TKey, TValue> SequenceBuilder<TKey, TValue>.expect(key: Any) = expectKey(orderBuilder.parseKey(key))

/**
 * Same as [expect] with single argument, but accepts multiple keys.
 * Order of the keys is ignored.
 *
 * @see [SequenceBuilder]
 */
fun <TKey, TValue> SequenceBuilder<TKey, TValue>.expect(vararg keys: Any) = par {
    for (k in keys) expect(k)
}

/**
 * Same as [add] but accepts a parsed key.
 *
 * @see [add]
 * @see [SequenceBuilder]
 */
fun <TKey, TValue> SequenceBuilder<TKey, TValue>.addKey(key: TKey, value: TValue) {
    expectKey(key)
    orderBuilder.register(key, value)
}

/**
 * Adds an item to the sequence.
 *
 * @param key key of the item
 * @param value the item to add
 *
 * @see [SequenceBuilder]
 */
fun <TKey, TValue> SequenceBuilder<TKey, TValue>.add(key: Any, value: TValue) = addKey(orderBuilder.parseKey(key), value)

/**
 * Adds an item with anonymous key to the sequence.
 *
 * @param value the item to add
 *
 * @see [SequenceBuilder]
 */
fun <TKey, TValue> SequenceBuilder<TKey, TValue>.add(value: TValue) {
    val t = orderBuilder.register(value)
    expectKey(t.key)
}
