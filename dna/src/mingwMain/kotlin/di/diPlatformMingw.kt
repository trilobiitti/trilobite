package com.github.trilobiitti.trilobite.dna.di

/* WTF-KOTLIN-MULTIPLATFORM-OF-THE-DAY: Why should I keep 3 copies of the piece of code to just add one annotation in
 * one of copies? Why cannot even platform-specific annotations be accessible from common code?
 */
@kotlin.native.ThreadLocal
private lateinit var currentDIInstance: DI

actual fun getCurrentDIInstance(): DI = currentDIInstance

actual fun setCurrentDIInstance(instance: DI) {
    currentDIInstance = instance
}
