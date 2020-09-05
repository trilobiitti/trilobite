package com.github.trilobiitti.trilobite.dna.di

class PlainDIContainer(
        private val map: MutableMap<DependencyKey, DependencyResolver> = mutableMapOf()
) : DI {
    override fun getResolver(key: DependencyKey): DependencyResolver =
            map[key] ?: throw UnknownDependencyException(key)

    override fun register(key: DependencyKey, resolver: DependencyResolver) {
        map[key] = resolver
    }
}
