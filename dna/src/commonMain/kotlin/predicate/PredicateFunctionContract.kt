package com.github.trilobiitti.trilobite.dna.predicate

import com.github.trilobiitti.trilobite.dna.data.DataType
import kotlin.reflect.KClass


interface PredicateFunctionContract {
    // Throws if argument types are not acceptable
    // Returns result type if they are
    fun validateArgumentTypes(inputs: List<PredicateFunctionInput>): DataType<*>
}


class NotValidPredicateFunctionInputError(val availableFunctionInputClasses: List<KClass<*>> )
    : Error("Not valid predicate function input. Allowed classes:" +
        " ${availableFunctionInputClasses.map { it.simpleName }.joinToString(separator = " ,") }}." +
        "Combination of these classes is not allowed")