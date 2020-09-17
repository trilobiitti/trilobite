package com.github.trilobiitti.trilobite.dna.testUtils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual fun runAsyncTest(block: suspend CoroutineScope.() -> Unit): dynamic = GlobalScope.promise(block = block)
