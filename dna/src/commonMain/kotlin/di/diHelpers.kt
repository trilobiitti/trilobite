package com.github.trilobiitti.trilobite.dna.di

import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.cast

fun DI.resolve(key: DependencyKey, vararg args: Any?) = this.getResolver(key)(args)

/**
 * Base class for objects that bind a dependency injectable through [DI] to a function-like object or delegated
 * variable.
 *
 * There is [bindDependency] function with few overloads (for different argument counts) creating a subclasses of this
 * class.
 *
 * Example:
 * ```kotlin
 * val createFooFromBar = bindDependency<Foo>("new foo from bar") { bar ->
 *    // Optional default resolver. If not provided, createFooFromBar will throw error when "new foo from bar"
 *    // dependency is not registered
 *    DefaultFooImpl(bar)
 * }
 *
 * // Now dependency can be resolved like this:
 * val myFoo = createFooFromBar(myBar)
 *
 * // which is equivalent to
 * val myFoo = DI.resolve("new foo from bar", myBar) as Foo
 * // But supports static typing and auto completion in IDE
 *
 * // New implementation can be registered in current DI container by invocation of use() method:
 * createFooFromBar.use { bar -> CustomFoo(bar) }
 *
 * // which is equivalent to
 * DI.register("new foo from bar") { args -> CustomFoo(args[0] as Bar) }
 *
 * // For zero-argument and single-argument resolvers property binding is also supported:
 * val Bar.asFoo by createFooFromBar
 * // now
 * val myFoo = myBar.asFoo
 * // will be equivalent to
 * val myFoo = DI.resolve("new foo from bar", myBar) as Foo
 *
 * // For zero-argument resolvers the property owner is ignored so it makes sense to use a global property:
 * var currentBar: Bar by bindDependency("current bar")
 * // setter is also implemented for zero-argument bindings, so
 * currentBar = myBar
 * // will be equivalent to
 * DI.register("current bar") { myBar }
 * ```
 *
 * Multiplatform warning: It's recommended to declare bindings inside a global object to make them accessible as
 * immutable objects on non-main threads of native applications.
 */
@OptIn(ExperimentalStdlibApi::class)
abstract class BaseDependencyBinding<T : Any, F : Function<T>>(
        val key: DependencyKey,
        protected val default: F?,
        protected val tCls: KClass<T>
)  {
    protected inline fun invoke0(argsAsArray: () -> Array<out Any?>, invokeDefault: (F) -> T): T {
        val resolver: DependencyResolver = try {
            DI.getResolver(key)
        } catch (e: UnknownDependencyException) {
            val default = this.default ?: throw e
            return invokeDefault(default)
        }

        return tCls.cast(resolver(argsAsArray()))
    }

    protected abstract fun fToResolver(f: F): DependencyResolver

    fun use(f: F) {
        DI.register(key, fToResolver(f))
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun <T : Any?> castArg(arg: Any?, clz: KClass<*>) = arg?.let { clz.cast(it) } as T

class VarargDependencyBinding<T : Any>(
        key: DependencyKey,
        default: ((args: Any?) -> T)?,
        tCls: KClass<T>
) : BaseDependencyBinding<T, (args: Any?) -> T>(key, default, tCls) {
    fun invoke(vararg args: Any?): T = invoke0({ args }) { it(args) }

    override fun fToResolver(f: (args: Any?) -> T): DependencyResolver = f
}

class DependencyBinding0<T : Any>(
        key: DependencyKey,
        default: (() -> T)?,
        tCls: KClass<T>
) : BaseDependencyBinding<T, () -> T>(key, default, tCls) {
    operator fun invoke(): T = invoke0(::emptyArray) { it() }

    override fun fToResolver(f: () -> T): DependencyResolver = { f() }

    /**
     * With this `DependencyBinding0` can be used as delegate for a variable.
     *
     * NOTE: This may become semantically incorrect if the dependency resolution result is different on every invocation
     * e.g. if associated [DependencyResolver] creates a new object.
     */
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T = invoke()

    /**
     * Even more questionable function than [DependencyBinding0.getValue]. Allows to use [DependencyBinding0] as
     * a delegate for mutable variable.
     *
     * Registers a [DependencyResolver] that always returns passed [value] as resolver for [key] in current DI
     * container.
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = use { value }
}

/**
 * @see BaseDependencyBinding
 */
inline fun <reified T : Any> bindDependency(
        key: DependencyKey,
        noinline default: (() -> T)? = null
) = DependencyBinding0(key, default, T::class)

class DependencyBinding1<T : Any, A0 : Any?>(
        key: DependencyKey,
        default: ((A0) -> T)?,
        tCls: KClass<T>,
        private val a0Cls: KClass<*>
) : BaseDependencyBinding<T, (A0) -> T>(key, default, tCls) {
    operator fun invoke(arg0: A0): T = invoke0({ arrayOf<Any?>(arg0) }) { it(arg0) }

    override fun fToResolver(f: (arg0: A0) -> T): DependencyResolver =
            { args -> f(castArg(args[0], a0Cls)) }

    /**
     * Hopefully, this might be useful for some tricks like
     *
     * ```
     * val fooToBar = bindDependency<Bar, Foo>(...)
     *
     * val Foo.asBar by fooToBar
     * ```
     */
    operator fun getValue(thisRef: A0, property: KProperty<*>): T = invoke(thisRef)
}

/**
 * @see BaseDependencyBinding
 */
inline fun <reified T : Any, reified A0 : Any?> bindDependency(
        key: DependencyKey,
        noinline default: ((arg0: A0) -> T)? = null
) = DependencyBinding1(key, default, T::class, A0::class)

class DependencyBinding2<T : Any, A0 : Any?, A1 : Any?>(
        key: DependencyKey,
        default: ((A0, A1) -> T)?,
        tCls: KClass<T>,
        private val a0Cls: KClass<*>,
        private val a1Cls: KClass<*>
) : BaseDependencyBinding<T, (A0, A1) -> T>(key, default, tCls) {
    operator fun invoke(arg0: A0, arg1: A1): T = invoke0({ arrayOf<Any?>(arg0, arg1) }) { it(arg0, arg1) }

    override fun fToResolver(f: (arg0: A0, arg1: A1) -> T): DependencyResolver =
            { args -> f(castArg(args[0], a0Cls), castArg(args[1], a1Cls)) }
}

/**
 * @see BaseDependencyBinding
 */
inline fun <reified T : Any, reified A0 : Any?, reified A1 : Any?> bindDependency(
        key: DependencyKey,
        noinline default: ((arg0: A0, arg1: A1) -> T)? = null
) = DependencyBinding2(key, default, T::class, A0::class, A1::class)

class DependencyBinding3<T : Any, A0 : Any?, A1 : Any?, A2 : Any?>(
        key: DependencyKey,
        default: ((A0, A1, A2) -> T)?,
        tCls: KClass<T>,
        private val a0Cls: KClass<*>,
        private val a1Cls: KClass<*>,
        private val a2Cls: KClass<*>
) : BaseDependencyBinding<T, (A0, A1, A2) -> T>(key, default, tCls) {
    operator fun invoke(arg0: A0, arg1: A1, arg2: A2): T = invoke0({ arrayOf<Any?>(arg0, arg1, arg2) }) {
        it(arg0, arg1, arg2)
    }

    override fun fToResolver(f: (arg0: A0, arg1: A1, arg2: A2) -> T): DependencyResolver =
            { args -> f(castArg(args[0], a0Cls), castArg(args[1], a1Cls), castArg(args[2], a2Cls)) }
}

/**
 * @see BaseDependencyBinding
 */
inline fun <reified T : Any, reified A0 : Any?, reified A1 : Any?, reified A2 : Any?> bindDependency(
        key: DependencyKey,
        noinline default: ((arg0: A0, arg1: A1, arg2: A2) -> T)? = null
) = DependencyBinding3(key, default, T::class, A0::class, A1::class, A2::class)
