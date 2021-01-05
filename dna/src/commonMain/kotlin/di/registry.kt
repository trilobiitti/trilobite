package com.github.trilobiitti.trilobite.dna.di

import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Key class used by [Registry] to emulate namespaces inside DI container.
 */
data class CompositeKey(
    val registryKey: Any,
    val itemKey: Any
)

/**
 * Exception thrown by [Registry.resolve] when requested item is missing in the registry.
 */
class UnregisteredRegistryItemException(
    registryKey: Any,
    itemName: Any
) : Exception("'$itemName' is not present in '$registryKey' registry.")

/**
 * An utility class that creates a namespace inside a DI container.
 *
 * Example:
 * ```
 * val databasesRegistry: Registry<Database> = Registry::new("database")
 * val resolveDatabase = bindDependency("get database", databasesRegistry.resolve)
 * val registerDatabase = bindDependency("register database", databasesRegistry.register)
 *
 * // Now "databases" can be added and resolved by calls of `registerDatabase` and `resolveDatabase`:
 * registerDatabase("main", MainDatabase(...))
 *
 * val knownTrilobites = resolveDatabase("main").collection("trilobites").findAll()
 *
 * // Items stored in the registry namespace have no collisions with items registered in a usual way unless one tries
 * very hard to create a collision:
 *
 * DI.register("main", {...})
 *
 * // This still returns object registered with `registerDatabase` call
 * val mainDb: Database = resolveDatabase("main")
 *
 * // When methods of Registry are registered as resolvers in DI container (which is recommended as Registry is rather a
 * // helper for resolver registration than a good way to access DI)...
 * resolveDatabase.useDefault()
 * registerDatabase.useDefault()
 * // ...they can be used by code that is not aware of registry instance and initial bindings:
 *
 * fun extermination() {
 *   DI.resolve<Database>("get database", "main").collection("trilobites").drop()
 * }
 * ```
 */
class Registry<T : Any>(
    private val registryKey: Any,
    private val clz: KClass<T>
) {
    companion object {
        /**
         * Creates a new [Registry].
         *
         * A separate function instead of constructor is necessary to infer item class [clz] from generic parameter.
         */
        inline fun <reified T : Any> new(registryKey: Any): Registry<T> = Registry(registryKey, T::class)
    }

    /**
     * Registers an item in the namespace.
     *
     * The function is implemented as a variable containing a lambda to be compatible with [bindDependency].
     */
    val register: (name: Any, fn: () -> T) -> Unit = { name: Any, fn: () -> T ->
        DI.register(
            CompositeKey(registryKey, name)
        ) { fn() }
    }

    /**
     * Resolves a namespace item.
     *
     * The function is implemented as a variable containing a lambda to be compatible with [bindDependency].
     *
     * @throws UnregisteredRegistryItemException when there is no such item
     */
    val resolve: (name: Any) -> T = { name: Any ->
        val resolver = try {
            DI.getResolver(CompositeKey(registryKey, name))
        } catch (e: UnknownDependencyException) {
            throw UnregisteredRegistryItemException(registryKey, name)
        }

        clz.cast(resolver(emptyArray()))
    }
}
