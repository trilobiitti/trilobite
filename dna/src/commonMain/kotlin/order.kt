package com.github.trilobiitti.trilobite.dna

interface OrderRegistrationToken {
    fun unregister()
}

interface OrderItemToken<TKey, TValue> : OrderRegistrationToken {
    val key: TKey
    val value: TValue
}

interface OrderLinkToken<TKey> : OrderRegistrationToken {
    val from: TKey
    val to: TKey
}

/**
 * Order defines order in which items of type [TValue] are ordered according to information about dependencies between
 * those items.
 *
 * Each item ordered by the Order is associated with a key of type [TKey]. Order of items associated with the same key
 * is not defined.
 */
interface Order<TKey, TValue> {
    /**
     * Creates a key from given object.
     *
     * @param source object to be transformed to key
     * @throws IllegalArgumentException if type of [source] is not supported by this implementation of [Order]
     */
    fun parseKey(source: Any): TKey

    /**
     * Register item with given key.
     *
     * Returned value can be used to retain key, item, or to unregister the item.
     *
     * @return token that allows unregistration of the item
     * @throws IllegalStateException if the item is already registered for this key
     */
    fun register(key: TKey, value: TValue): OrderItemToken<TKey, TValue>

    /**
     * Just like [register] but generates a random unique key.
     *
     * The key is unique but can be extracted from token and used to register other items.
     */
    fun register(value: TValue): OrderItemToken<TKey, TValue>

    /**
     * Registers a link (dependency) between two items.
     *
     * @param from key of the item that should come before [to]
     * @param to key of the item that should come after [from]
     * @return token by which the link can be removed
     */
    fun link(from: TKey, to: TKey): OrderLinkToken<TKey>

    /**
     * Ensures that the order is valid (doesn't contain loops).
     *
     * @throws IllegalStateException if this order contains loops
     */
    fun validate()

    /**
     * Executes passed callback for each key registered in this order.
     *
     * The callback receives 3 arguments:
     * - the key
     * - list of all values associated with it
     * - values returned by the callback for all keys the given key depends on
     *
     * This can, for example, be used to execute concurrently some jobs when [TValue] is a job/function
     * [T] is some sort of promise: the callback should launch an async job that awaits for completion of
     * all received promises, then executes the functions associated with a key (the callback should
     * synchronously return promise of that job's completion).
     *
     * @throws IllegalStateException if this order contains loops
     */
    fun <T> visit(cb: (TKey, Iterable<TValue>, Iterable<T>) -> T): Iterable<T>

    /**
     * Returns items ordered by this order.
     *
     * @throws IllegalStateException if this order contains loops
     */
    fun toLinearList(): List<TValue>
}
