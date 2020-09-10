package com.github.trilobiitti.trilobite.dna.testHelpers

import com.github.trilobiitti.trilobite.dna.di.DI
import com.github.trilobiitti.trilobite.dna.di.PlainDIContainer
import kotlin.test.BeforeTest

interface DIAwareTest {
    @BeforeTest
    fun initDI() {
        // TODO: This ain't gonna work for tests running concurrently, so implementation should be changed for platforms supporting concurrency
        DI.instance = PlainDIContainer()
    }
}
