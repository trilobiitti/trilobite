package com.github.trilobiitti.trilobite.dna.data.predicate

import com.github.trilobiitti.trilobite.dna.data.DataType
import com.github.trilobiitti.trilobite.dna.data.DataTypes
import com.github.trilobiitti.trilobite.dna.data.SimpleValidationError

/**
 * Contract of standard `"str"` predicate language function.
 *
 * This function takes a literal input and returns it's text as a string value.
 *
 * One can ask: "Why not interpret literals in predicate language as strings by default?"
 * And the short answer will be: unambiguity and consistency.
 *
 * The long answer is: the Trilobite predicate definition language tries to be as unambiguous as possible while
 * supporting as wide range of serialization methods as possible. JSON is a canonical serialization method but it
 * is not the only one. Some serialization methods make no difference between numbers and strings: when you see
 * `page=42` in URL you don't actually know if value of "page" parameter is interpreted as a number or as a string
 * (yet you can assume it is a number, because of some context, but machines and most of humans are stupid and don't
 * know any context). So, the Trilobite predicate language forces any user to specify a type of each constant by
 * applying appropriate function ("str", "num", etc.) to a literal to convert it into a value.
 */
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
