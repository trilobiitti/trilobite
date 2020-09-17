package com.github.trilobiitti.trilobite.dna.di

@kotlin.native.ThreadLocal
private var currentDIInstance: DI = NullDIContainer

actual fun getCurrentDIInstance(): DI = currentDIInstance

actual fun setCurrentDIInstance(instance: DI) {
    currentDIInstance = instance
}
