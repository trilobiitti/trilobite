package com.github.trilobiitti.trilobite.dna.plugins

import com.github.trilobiitti.trilobite.dna.DefaultOrder

/**
 * Loads plugins sequentially invoking [InitializationStage]s.
 */
suspend fun loadPluginsSequentially(plugins: Iterable<Plugin>) {
    val initializationOrder = DefaultOrder<InitializationStage>()

    for (plugin in plugins) plugin.apply(initializationOrder)

    for (stage in initializationOrder.toLinearList()) stage()
}

/**
 * Loads provided [plugins][Plugin] trying to load them even if dependencies of some [initialization stages][InitializationStage]
 * aren't configured properly.
 *
 * This function is intended mostly for development/debug.
 * If an application cannot launch with standard plugin loader (like [loadPluginsSequentially]) because of issues with
 * initialization stage dependencies it should not be considered as ready for production use.
 *
 * `loadPluginsRestoringActionSequence` tries to provide as much information as possible about possible misconfigurations:
 * lists of possible missing dependencies, error stack traces.
 *
 * @param plugins the plugins to load
 *
 * TODO: Create custom exception or use some more specific standard exception
 * @throws Exception if some of initialization stages cannot be performed at all
 * @throws Exception if initialization stage dependencies contain loops
 * @throws Exception if [Plugin.apply] method of one of plugins throws
 */
suspend fun loadPluginsRestoringActionSequence(plugins: Iterable<Plugin>) {
    val initializationOrder = DefaultOrder<InitializationStage>()

    for (plugin in plugins) {
        plugin.apply(initializationOrder)

        try {
            initializationOrder.validate()
        } catch (e: Exception) {
            throw Exception(
                "Plugin $plugin introduces error in initialization sequence order",
                e
            )
        }
    }

    val orderedStages = mutableListOf<Pair<Any, InitializationStage>>()
    var failedStages = mutableSetOf<Triple<Any, InitializationStage, Exception>>()

    initializationOrder.visit<Unit> { key, stages, _ -> orderedStages.addAll(stages.map { key to it }) }

    for (stage in orderedStages) {
        try {
            stage.second()
        } catch (e: Exception) {
            failedStages.add(Triple(stage.first, stage.second, e))
            continue
        }

        val successfulStages = mutableListOf(stage)

        retryFailed@ while (failedStages.isNotEmpty()) {
            val refailedStages = mutableSetOf<Triple<Any, InitializationStage, Exception>>()

            for ((retryKey, retryStage, prevException) in failedStages) {
                if (try {
                    retryStage()

                    true
                } catch (e: Exception) {
                        if (e.message != prevException.message) {
                            println(
                                    "[WARN] Initialization stage $retryKey ($retryStage) may have undeclared dependency" +
                                        " on one of the following stages: ${
                                        successfulStages.joinToString { "${it.first} (${it.second})" }
                                        }. Before completion of those stages it did throw the following exception:"
                                )
                            println(prevException)
                            println("... but now it has thrown:")
                            println(e)
                        }

                        refailedStages.add(Triple(retryKey, retryStage, e))

                        false
                    }
                ) {
                    println(
                        "[WARN] Initialization stage $retryKey ($retryStage) may have undeclared dependency on one of the following stages: ${
                        successfulStages.joinToString { "${it.first} (${it.second})" }
                        }. Before completion of those stages it did fail with the following exception:"
                    )
                    println(prevException)

                    successfulStages.add(retryKey to retryStage)
                }
            }

            val anyRetrySucceed = refailedStages.size == failedStages.size

            failedStages = refailedStages

            if (!anyRetrySucceed) {
                break@retryFailed
            }
        }
    }

    if (failedStages.isNotEmpty()) {
        for ((key, stage, error) in failedStages) {
            println("[ERROR] Stage $key ($stage) failed with the following exception:")
            println(error)
        }

        throw Exception(
            "[ERROR] Some initialization stages have failed: ${
            failedStages.joinToString { "${it.first} (${it.second})" }
            }"
        )
    }
}
