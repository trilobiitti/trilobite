package com.github.trilobiitti.trilobite.dna

/*
    Interfaces
 */

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

interface Order<TKey, TValue> {
    fun parseKey(source: Any): TKey

    fun register(key: TKey, value: TValue): OrderItemToken<TKey, TValue>

    fun register(value: TValue): OrderItemToken<TKey, TValue>

    fun link(from: TKey, to: TKey): OrderLinkToken<TKey>

    fun validate()

    fun <T> visit(cb: (TKey, Iterable<TValue>, Iterable<T>) -> T): Iterable<T>

    fun toLinearList(): List<TValue>
}

/*
    Helpers, DSLs
 */

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

/*
    Default implementation
 */

abstract class DefaultKey {
    open fun <TValue> onRegistered(order: DefaultOrder<TValue>) {}
}

class UniqueKey : DefaultKey()

data class ConstantKey(val id: Any) : DefaultKey()

data class PostKey(val key: DefaultKey) : DefaultKey() {
    override fun <TValue> onRegistered(order: DefaultOrder<TValue>) {
        order.link(key, this)
    }
}

data class PreKey(val key: DefaultKey) : DefaultKey() {
    override fun <TValue> onRegistered(order: DefaultOrder<TValue>) {
        order.link(this, key)
    }
}

class DefaultOrder<TValue> : Order<DefaultKey, TValue> {
    companion object {
        private val PRE_KEY_RE = Regex("^pre[-: ]?(.+)$", RegexOption.IGNORE_CASE)
        private val POST_KEY_RE = Regex("^post[-: ]?(.+)$", RegexOption.IGNORE_CASE)
    }

    private fun parseStringKey(source: String): DefaultKey {
        PRE_KEY_RE.matchEntire(source)?.let { match ->
            return PreKey(
                    parseStringKey(match.groupValues[1])
            )
        }
        POST_KEY_RE.matchEntire(source)?.let { match ->
            return PostKey(
                    parseStringKey(match.groupValues[1])
            )
        }
        return ConstantKey(source)
    }

    override fun parseKey(source: Any): DefaultKey = when (source) {
        is DefaultKey -> source
        is String -> parseStringKey(source)
        else -> throw IllegalArgumentException("Illegal key source $source")
    }

    private class LoopException : Exception() {
        val keys = mutableListOf<Any>()

        override val message: String?
            get() = "Found a loop containing: ${keys.joinToString(", ") { "\"$it\"" }}"
    }

    private inner class Node(
            val key: DefaultKey,
            val values: MutableSet<TValue>
    ) {
        var mark = 0
        var dependencies = mutableSetOf<Link>()

        fun linearize(linearList: MutableList<TValue>) {
            when (mark) {
                1 -> throw LoopException()
                2 -> return
                else -> {
                    mark = 1

                    try {
                        for (d in dependencies) d.fromNode.linearize(linearList)
                    } catch (e: LoopException) {
                        e.keys.add(key)
                        throw e
                    }

                    linearList.addAll(values)

                    mark = 2
                }
            }
        }
    }

    private inner class ItemToken(
            private val node: Node,
            override val value: TValue
    ) : OrderItemToken<DefaultKey, TValue> {
        override val key get() = node.key

        override fun unregister() {
            if (!node.values.remove(value)) {
                throw IllegalStateException("Value is already unregistered")
            }

            linearized = null
        }
    }

    private val nodes: MutableMap<DefaultKey, Node> = mutableMapOf()
    private var linearized: List<TValue>? = null

    private fun getNode(key: DefaultKey): Node {
        var created = false
        val node = nodes.getOrPut(key, {
            created = true
            return@getOrPut Node(key, mutableSetOf())
        })

        if (created) key.onRegistered(this)

        return node
    }

    override fun register(key: DefaultKey, value: TValue): OrderItemToken<DefaultKey, TValue> {
        val node = getNode(key)

        if (!node.values.add(value)) {
            throw IllegalStateException("Value is already registered")
        }

        return ItemToken(node, value)
    }

    override fun register(value: TValue): OrderItemToken<DefaultKey, TValue> = register(UniqueKey(), value)

    private inner class Link(
            val fromNode: Node,
            val toNode: Node
    ) : OrderLinkToken<DefaultKey> {
        override val from: DefaultKey
            get() = fromNode.key
        override val to: DefaultKey
            get() = toNode.key

        override fun unregister() {
            if (!toNode.dependencies.remove(this)) {
                throw IllegalStateException("Link already destroyed")
            }

            linearized = null
        }
    }

    override fun link(from: DefaultKey, to: DefaultKey): OrderLinkToken<DefaultKey> {
        val link = Link(getNode(from), getNode(to))

        getNode(to).dependencies.add(link)

        linearized = null

        return link
    }

    private fun linearize(): List<TValue> = linearized ?: run {
        val nodes = this.nodes.values
        for (n in nodes) n.mark = 0
        val lst = mutableListOf<TValue>()
        for (n in nodes) n.linearize(lst)

        linearized = lst
        lst
    }

    override fun validate() {
        if (linearized == null) linearize()
    }

    override fun toLinearList(): List<TValue> = linearized ?: linearize()

    override fun <T> visit(cb: (DefaultKey, Iterable<TValue>, Iterable<T>) -> T): Iterable<T> {
        validate()

        val results = mutableMapOf<DefaultKey, T>()
        val finalResults = mutableMapOf<DefaultKey, T>()

        fun visitNode(n: Node): T {
            val present = results[n.key]
            if (present !== null) {
                finalResults.remove(n.key)
                return present
            }

            val res = cb(n.key, n.values, n.dependencies.map { visitNode(it.fromNode) })
            results[n.key] = res
            finalResults[n.key] = res
            return res
        }

        for (n in nodes.values) visitNode(n)

        return finalResults.values
    }
}
