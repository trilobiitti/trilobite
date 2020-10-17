package com.github.trilobiitti.trilobite.dna.data.predicate

import com.github.trilobiitti.trilobite.dna.data.DataType
import com.github.trilobiitti.trilobite.dna.data.ValidationError


interface PredicateFunctionContract {

    /**
     * @throws ValidationError if argument types are not acceptable
     * @return  result type if they are
     */

    fun validateArgumentTypes(inputs: List<PredicateFunctionInput>): DataType<*>
}
