package com.github.trilobiitti.trilobite.dna.testHelpers

import com.github.trilobiitti.trilobite.dna.di.DI
import com.github.trilobiitti.trilobite.dna.di.PlainDIContainer
import kotlin.test.BeforeTest

interface DIAwareTest {
    @BeforeTest
    fun initDI() {
        DI.instance = PlainDIContainer()

        initDependencies()
    }

    fun initDependencies() {}
}
