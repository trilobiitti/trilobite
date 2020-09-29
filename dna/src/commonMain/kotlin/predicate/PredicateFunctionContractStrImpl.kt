package com.github.trilobiitti.trilobite.dna.predicate

import com.github.trilobiitti.trilobite.dna.data.DataType

class PredicateFunctionContractStrImpl: PredicateFunctionContract {

    private fun isUseLiteralStrategy (inputs:  List<PredicateFunctionInput> ) = inputs.all { it is LiteralInput  };
    private fun isUseValueStrategy (inputs:  List<PredicateFunctionInput> ) = inputs.all { it is ValueInput<*>  };


    private fun literalStrategy(inputs:  List<PredicateFunctionInput>): Nothing = TODO();
    private fun valueStrategy(inputs:  List<PredicateFunctionInput>): Nothing = TODO();


    override fun validateArgumentTypes(inputs: List<PredicateFunctionInput>): DataType<*>  = when {
        isUseLiteralStrategy(inputs) ->  literalStrategy(inputs)
        isUseValueStrategy(inputs) ->  valueStrategy(inputs)
        else -> throw NotValidPredicateFunctionInputError(availableFunctionInputClasses = listOf(LiteralInput::class, ValueInput::class ))
    }
}