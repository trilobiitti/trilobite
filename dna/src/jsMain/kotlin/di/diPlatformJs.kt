package com.github.trilobiitti.trilobite.dna.di

private var currentDIInstance: DI = UninitializedDIContainer

actual fun getCurrentDIInstance(): DI = currentDIInstance

actual fun setCurrentDIInstance(instance: DI) {
    currentDIInstance = instance
}
