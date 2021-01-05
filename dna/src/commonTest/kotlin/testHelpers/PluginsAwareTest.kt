package com.github.trilobiitti.trilobite.dna.testHelpers

import com.github.trilobiitti.trilobite.dna.plugins.Plugin
import com.github.trilobiitti.trilobite.dna.plugins.loadPlugins
import com.github.trilobiitti.trilobite.dna.testUtils.runAsyncTest
import kotlin.test.BeforeTest

interface PluginsAwareTest {
    @BeforeTest
    fun setup() = runAsyncTest {
        loadPlugins(getPlugins())
    }

    fun getPlugins(): Iterable<Plugin> = emptyList()
}
