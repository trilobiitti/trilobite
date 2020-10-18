package com.github.trilobiitti.trilobite.dna.data.predicate

import com.github.trilobiitti.trilobite.dna.data.DataType

/**
 * Models an input of a function of predicate definition language.
 */
sealed class PredicateFunctionInput

/**
 * Models a literal string passed to predicate language function.
 *
 * E.g. for predicate fragment `["str", "Hello"]` inputs of `"str"` function are modelled by
 * `listOf(LiteralInput("Hello"))`.
 *
 * @property literal text of the string literal
 */
class LiteralInput(val literal: String): PredicateFunctionInput()

/**
 * Models a value returned by a predicate language function that is passed as an input to another function.
 *
 * E.g. for predicate fragment `["lowercase", ["str", "Hello"]]` inputs of `"lowercase"` function are modelled by
 * `listOf(ValueInput(DataTypes.STRING))`.
 *
 * @property type type of the value passed, represented as [DataType]
 */
class ValueInput<T>(val type: DataType<T>): PredicateFunctionInput()
