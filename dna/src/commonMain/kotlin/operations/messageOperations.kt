package com.github.trilobiitti.trilobite.dna.operations

import com.github.trilobiitti.trilobite.dna.data.Document

typealias Message = Document

typealias MessageOperationStage = suspend (Message) -> Message

typealias MessageOperationExecutor = OperationPlanExecutor<Message, MessageOperationStage>

object MessageOperations : OperationRegistry<Message, MessageOperationStage>(
    registryKey = "message operation",
    registrationKey = "register message operation",
    editKey = "edit message operation",
    executorKey = "message operation executor"
)
