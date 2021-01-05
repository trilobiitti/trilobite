package com.github.trilobiitti.trilobite.dna.operations

import com.github.trilobiitti.trilobite.dna.add
import com.github.trilobiitti.trilobite.dna.data.DocumentFieldKey
import com.github.trilobiitti.trilobite.dna.data.MapDocument
import com.github.trilobiitti.trilobite.dna.di.PlainRootDIPlugin
import com.github.trilobiitti.trilobite.dna.expect
import com.github.trilobiitti.trilobite.dna.plugins.Plugin
import com.github.trilobiitti.trilobite.dna.testHelpers.PluginsAwareTest
import com.github.trilobiitti.trilobite.dna.testUtils.runAsyncTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class MessageOperationsTest : PluginsAwareTest {
    override fun getPlugins(): Iterable<Plugin> = listOf(
        PlainRootDIPlugin(),
        DefaultMessageOperationsPlugin(),
        SequentialMessageOperationsExecutorPlugin(),
    )

    @Test
    fun testSimpleOperation() = runAsyncTest {
        MessageOperations.edit("test-op") {
            alwaysAdd {
                add("double") {
                    it[DocumentFieldKey("x")] = (it[DocumentFieldKey("x")] as Number).toFloat() * 2.0f
                    it
                }
            }

            alwaysAdd {
                add("increment") {
                    it[DocumentFieldKey("x")] = (it[DocumentFieldKey("x")] as Number).toFloat() + 1f
                    it
                }

                expect("double")
            }

            flush()
        }

        val inputMessage = MapDocument()
        inputMessage[DocumentFieldKey("x")] = 42f

        val outputMessage = MessageOperations.resolve("test-op").execute(inputMessage)

        assertEquals(
            86f,
            outputMessage[DocumentFieldKey("x")]
        )

        assertSame(
            inputMessage,
            outputMessage,
            "Message is expected to be mutable for default message operations executor"
        )
    }
}
