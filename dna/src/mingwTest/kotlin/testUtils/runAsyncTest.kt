package com.github.trilobiitti.trilobite.dna.testUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

actual fun runAsyncTest(block: suspend CoroutineScope.() -> Unit) = runBlocking(block = block)
