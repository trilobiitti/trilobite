package com.github.trilobiitti.trilobite.dna.operations

import com.github.trilobiitti.trilobite.dna.add
import com.github.trilobiitti.trilobite.dna.plugins.DefaultPlugin

class DefaultMessageOperationsPlugin : DefaultPlugin({
    add("default message operations management") {
        MessageOperations.useDefaults()
        DefaultOperationManager.install(MessageOperations)
    }
})

class SequentialMessageOperationsExecutorPlugin : DefaultPlugin({
    add("default message operations executor") {
        val ex: MessageOperationExecutor = SequentialOperationPlanExecutor { stage, context -> stage(context) }
        MessageOperations.executor.use { ex }
    }
})
