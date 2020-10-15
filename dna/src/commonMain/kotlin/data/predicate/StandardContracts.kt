package com.github.trilobiitti.trilobite.dna.data.predicate

import com.github.trilobiitti.trilobite.dna.data.DataType
import com.github.trilobiitti.trilobite.dna.data.DataTypes
import com.github.trilobiitti.trilobite.dna.data.SimpleValidationError
import com.github.trilobiitti.trilobite.dna.predicate.LiteralInput
import com.github.trilobiitti.trilobite.dna.predicate.PredicateFunctionContract
import com.github.trilobiitti.trilobite.dna.predicate.PredicateFunctionInput

object StrFunctionContract : PredicateFunctionContract {
    private fun inputSizeValidation(inputs: List<PredicateFunctionInput>) {
        if (inputs.isEmpty() || inputs.size > 1) {
            throw SimpleValidationError("One parameter must be passed");
        }
    }

    private fun inputTypeValidation(inputs: List<PredicateFunctionInput>) {
        if (!inputs.all { it is LiteralInput }) {
            throw SimpleValidationError("Only ${LiteralInput::class.simpleName}  are accepted");
        }
    }

    override fun validateArgumentTypes(inputs: List<PredicateFunctionInput>): DataType<*> {
        inputSizeValidation(inputs);
        inputTypeValidation(inputs);

        return DataTypes.STRING

    }
}

