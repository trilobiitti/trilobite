package com.github.trilobiitti.trilobite.dna.predicate

import com.github.trilobiitti.trilobite.dna.data.DataTypes
import com.github.trilobiitti.trilobite.dna.data.NullableType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class PredicateStrFunctionContractTest {

    private val predicateStrFunctionContract = PredicateFunctionContractStrImpl();


    @Test
    fun shouldThrowsWhenPassNotValidPredicateFunctionInput(){

        assertFailsWith(NotValidPredicateFunctionInputError::class) {
            predicateStrFunctionContract.validateArgumentTypes(listOf(LiteralInput("updateAt"), ValueInput(NullableType(DataTypes.NUMBER)) ))
        }
    }

    @Test
    fun shouldReturnNullableStringDataTypeWhenPassValidPredicateFunctionInput() {
        val dataType = predicateStrFunctionContract.validateArgumentTypes(listOf(LiteralInput("name")))

        assertEquals(NullableType(DataTypes.STRING), dataType)
    }
}