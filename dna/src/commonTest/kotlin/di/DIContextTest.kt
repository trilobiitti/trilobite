package com.github.trilobiitti.trilobite.dna.di

import com.github.trilobiitti.trilobite.dna.testUtils.runAsyncTest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals

class DIContextTest {
    @Test
    fun shouldWithDIApplyDIInstance() = runAsyncTest {
        val di1 = PlainDIContainer().apply { register("dep") { "val1" } }
        val di2 = PlainDIContainer().apply { register("dep") { "val2" } }

        lateinit var j0: Job
        lateinit var j1: Job

        this.withDI(di1) {
            assertEquals("val1", DI.resolve("dep"))

            j0 = launch {
                assertEquals("val1", DI.resolve("dep"))
                delay(1)
                assertEquals("val1", DI.resolve("dep"))

                withDI(di2) {
                    assertEquals("val2", DI.resolve("dep"))

                    j1 = launch {
                        assertEquals("val2", DI.resolve("dep"))
                        delay(1)
                        assertEquals("val2", DI.resolve("dep"))
                    }

                    assertEquals("val2", DI.resolve("dep"))
                }

                assertEquals("val1", DI.resolve("dep"))
            }
            assertEquals("val1", DI.resolve("dep"))
        }

        j0.join()
        j1.join()
    }
}
