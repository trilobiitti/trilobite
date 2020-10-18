package com.github.trilobiitti.trilobite.dna.di

class PlainDIContainer(
    private val map: MutableMap<DependencyKey, DependencyResolver> = mutableMapOf(),
    private val parent: DI = NullDIContainer
) : DI {
    override fun getResolver(key: DependencyKey): DependencyResolver =
        map[key] ?: parent.getResolver(key)

    override fun register(key: DependencyKey, resolver: DependencyResolver) {
        map[key] = resolver
    }
}

object NullDIContainer : DI {
    override fun getResolver(key: DependencyKey): DependencyResolver =
        throw UnknownDependencyException(key)

    override fun register(key: DependencyKey, resolver: DependencyResolver) =
        throw IllegalStateException("Container does not support registration")
}

object UninitializedDIContainer : DI {
    private inline fun err(): Nothing =
        throw IllegalStateException("Container is not initialized")

    override fun getResolver(key: DependencyKey): DependencyResolver = err()

    override fun register(key: DependencyKey, resolver: DependencyResolver) = err()
}
