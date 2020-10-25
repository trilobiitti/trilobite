package com.github.trilobiitti.trilobite.dna.data.predicate

import com.github.trilobiitti.trilobite.dna.data.DataType

sealed class PredicateFunctionInput

class LiteralInput(val literal: String): PredicateFunctionInput()
class ValueInput<T>(val type: DataType<T>): PredicateFunctionInput()

