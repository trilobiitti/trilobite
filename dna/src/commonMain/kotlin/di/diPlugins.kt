package com.github.trilobiitti.trilobite.dna.di

import com.github.trilobiitti.trilobite.dna.add
import com.github.trilobiitti.trilobite.dna.expect
import com.github.trilobiitti.trilobite.dna.independently
import com.github.trilobiitti.trilobite.dna.plugins.DefaultPlugin

/**
 * Plugin that installs a new instance of [PlainDIContainer] as current instance of DI container.
 */
class PlainRootDIPlugin : DefaultPlugin({
    independently {
        add("di") {
            DI.instance = PlainDIContainer()
        }

        expect(DefaultPlugin.START_BARRIER)
    }
})
