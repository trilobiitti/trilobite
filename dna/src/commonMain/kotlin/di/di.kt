package com.github.trilobiitti.trilobite.dna.di

/**
 * Type of a key strategies are associated with in [DI].
 */
typealias DependencyKey = Any

/**
 * Type of strategy stored in [DI].
 */
typealias DependencyResolver = (args: Array<out Any?>) -> Any

class UnknownDependencyException(val key: DependencyKey) : Exception("Unknown dependency key: '$key'")

/**
 * Service locator that provides dependency resolution.
 *
 * Dependency resolution strategies (implemented as [DependencyResolver]s) are stored in a key-value storage (container)
 * implementing this interface. Companion object of this interface also implements it and delegates all calls
 * to an instance of [DI] assigned to [DI.instance]. That instance may be a storage itself or forward calls to
 * other DI instances depending on execution context (e.g. to an instance acquired from thread-local variable).
 *
 * *I would like to implement a cross-platform version of DI that uses current coroutine context but unfortunately
 * `coroutineContext` variable is not accessible from non-suspend functions (and I don't want dependency resolution
 * to be suspending for now) so such implementation (using `ThreadLocal.asContextElement`) will be there for JVM only.*
 */
interface DI {
    /**
     * Returns a resolution strategy for dependency with given name.
     *
     * @throws UnknownDependencyException if no resolvers are registered for given key
     */
    fun getResolver(key: DependencyKey): DependencyResolver

    /**
     * Registers a dependency resolver for key [key].
     *
     * @throws IllegalStateException if a resolver cannot be registered in current state of container
     */
    fun register(key: DependencyKey, resolver: DependencyResolver)

    companion object : DI {
        var instance: DI
            get() = getCurrentDIInstance()
            set(value) = setCurrentDIInstance(value)

        override fun getResolver(key: DependencyKey): DependencyResolver =
                instance.getResolver(key)

        override fun register(key: DependencyKey, resolver: DependencyResolver) =
                instance.register(key, resolver)
    }
}

expect fun getCurrentDIInstance(): DI
expect fun setCurrentDIInstance(instance: DI)
