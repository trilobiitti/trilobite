package com.github.trilobiitti.trilobite.dna.testUtils

import kotlinx.coroutines.CoroutineScope

expect fun runAsyncTest(block: suspend CoroutineScope.() -> Unit): Unit
