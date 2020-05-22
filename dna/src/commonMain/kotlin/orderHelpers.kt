package com.github.trilobiitti.trilobite.dna

operator fun <TKey, TValue> Order<TKey, TValue>.set(key: Any, value: TValue) {
    register(parseKey(key), value)
}

operator fun <TKey, TValue> Order<TKey, TValue>.plusAssign(p: Pair<Any, Any>) {
    link(parseKey(p.first), parseKey(p.second))
}

@DslMarker
annotation class OrderItemDsl

@OrderItemDsl
class OrderItemBuilder<TKey, TValue>(
        val order: Order<TKey, TValue>,
        val key: TKey
) {
    val tokens = mutableListOf<OrderRegistrationToken>()
}

fun <TKey, TValue> OrderItemBuilder<TKey, TValue>.before(k: Any) {
    tokens.add(order.link(key, order.parseKey(k)))
}

fun <TKey, TValue> OrderItemBuilder<TKey, TValue>.after(k: Any) {
    tokens.add(order.link(order.parseKey(k), key))
}

fun <TKey, TValue : Function<*>> OrderItemBuilder<TKey, TValue>.exec(function: TValue) {
    tokens.add(order.register(key, function))
}

// TODO: Declare it the way that tokens stored in OrderItemBuilder can be used later
inline operator fun <TKey, TValue> Order<TKey, TValue>.set(
        key: Any,
        block: OrderItemBuilder<TKey, TValue>.() -> Unit
) = OrderItemBuilder(this, parseKey(key)).run(block)
