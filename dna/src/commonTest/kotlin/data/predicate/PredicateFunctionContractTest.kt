package com.github.trilobiitti.trilobite.dna.data.predicate

import com.github.trilobiitti.trilobite.dna.data.DataTypes
import com.github.trilobiitti.trilobite.dna.data.SimpleValidationError
import com.github.trilobiitti.trilobite.dna.predicate.LiteralInput
import com.github.trilobiitti.trilobite.dna.predicate.ValueInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class PredicateStrFunctionContractTest {


    @Test
    fun shouldThrowsWhenPassNotOnePredicateFunctionInput() {
        assertFailsWith(SimpleValidationError::class) {
            StrFunctionContract.validateArgumentTypes(listOf())
        }

        assertFailsWith(SimpleValidationError::class) {
            StrFunctionContract.validateArgumentTypes(listOf(LiteralInput("literal 1"), LiteralInput("literal 2")))
        }
    }

    @Test
    fun shouldThrowsWhenPassNotValidPredicateFunctionInput() {
        assertFailsWith(SimpleValidationError::class) {
            StrFunctionContract.validateArgumentTypes(listOf(ValueInput(DataTypes.NUMBER)))
        }
    }


    @Test
    fun shouldReturnStringDataTypeWhenPassValidPredicateFunctionInput() {

        assertEquals(DataTypes.STRING,
                StrFunctionContract.validateArgumentTypes(listOf(LiteralInput("literal 1")))
        )
    }
}

