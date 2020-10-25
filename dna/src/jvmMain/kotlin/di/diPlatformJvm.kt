package com.github.trilobiitti.trilobite.dna.di

private val currentDIInstance: ThreadLocal<DI> = ThreadLocal.withInitial { UninitializedDIContainer }

actual fun getCurrentDIInstance(): DI = currentDIInstance.get()

actual fun setCurrentDIInstance(instance: DI) {
    currentDIInstance.set(instance)
}
