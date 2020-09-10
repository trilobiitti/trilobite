package com.github.trilobiitti.trilobite.dna.di

private lateinit var currentDIInstance: DI

actual fun getCurrentDIInstance(): DI = currentDIInstance

actual fun setCurrentDIInstance(instance: DI) {
    currentDIInstance = instance
}
