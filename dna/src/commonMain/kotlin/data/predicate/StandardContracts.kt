package com.github.trilobiitti.trilobite.dna.data.predicate

import com.github.trilobiitti.trilobite.dna.data.DataType
import com.github.trilobiitti.trilobite.dna.data.DataTypes
import com.github.trilobiitti.trilobite.dna.data.SimpleValidationError

object StrFunctionContract : PredicateFunctionContract {

    override fun validateArgumentTypes(inputs: List<PredicateFunctionInput>): DataType<*> {
        if (inputs.isEmpty() || inputs.size > 1) {
            throw SimpleValidationError("One parameter must be passed");
        }

        if (inputs.first() !is LiteralInput) {
            throw SimpleValidationError("Only literal parameter  can be  passed");
        }

        return DataTypes.STRING

    }
}


