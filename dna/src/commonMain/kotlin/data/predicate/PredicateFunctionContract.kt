package com.github.trilobiitti.trilobite.dna.data.predicate

import com.github.trilobiitti.trilobite.dna.data.DataType
import com.github.trilobiitti.trilobite.dna.data.ValidationError

/**
 * Describes behavior of certain predicate language function.
 *
 * Main purpose of this interface is to provide a way to validate predicate definitions without actually
 * evaluating them.
 */
interface PredicateFunctionContract {
    /**
     * Checks if given list of arguments (represented as [PredicateFunctionInput]) is valid for the function
     * described by this contract.
     *
     * For example, when validating predicate definition
     * ```
     * ["eq", ["field", "foo"], ["str", "bar"]]
     * ```
     * predicate definition validator should do the following:
     * - call [validateArgumentTypes] of `"field"` function contract with `listOf(LiteralInput("foo"))` as list
     *      of arguments; assume the contract returns `NullableType(DataTypes.ANY)`
     * - call [validateArgumentTypes] fo `"str"` function contract with `listOf(LiteralInput("foo"))` as list
     *      of arguments; the contract will return `DataTypes.STRING`
     * - call [validateArgumentTypes] of `"eq"` function with inputs of types computed from previous two calls
     *      as list of arguments: `listOf(ValueInput(NullableType(DataTypes.ANY)), ValueInput(DataTypes.STRING))`;
     *      argument count is correct and it is ok to compare `Any?` to `String` so `"eq"` contract will not throw
     *      and will return `DataTypes.BOOLEAN`
     * - a boolean value is exactly what is expected to be returned from a predicate so predicate definition
     *      validator will return without throwing any errors
     *
     * but when validating
     * ```
     * ["eq", ["str", "bar"]]
     * ```
     * [validateArgumentTypes] of `"eq"` function contract will be called with `listOf(ValueInput(DataTypes.STRING))`
     * which will make it throw [ValidationError] that will be thrown from predicate definition validator (potentially,
     * depending on predicate definition validator implementation, being wrapped into another exception).
     *
     * @throws ValidationError if arguments are not acceptable
     * @return type of a value returned by the function with given argument types
     */
    fun validateArgumentTypes(inputs: List<PredicateFunctionInput>): DataType<*>
}
