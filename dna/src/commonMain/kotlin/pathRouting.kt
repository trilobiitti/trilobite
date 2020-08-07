package com.github.trilobiitti.trilobite.dna


/**
 * Parses a path (C.O.)
 *
 * @param path a path represented as [String]
 * @return path represented as list of steps
 */
fun parsePath(path: String): List<String> = path
        .split('/')
        .filter(String::isNotEmpty)

private data class ParsedPathVariable<TIn : DeciderInputBase>(
        private val pathVariable: DecisionVariable<TIn, String>
) : DecisionVariable<TIn, List<String>> {
    override fun getFrom(context: DecisionContext<TIn>): List<String> = parsePath(context[pathVariable])
    override fun getDependencies(): List<DecisionVariable<TIn, *>> = listOf(pathVariable)
    override fun isMetaVariable(): Boolean = true
    override fun toString(): String = "path parsed from $pathVariable"
}

private data class PathStepVariable<TIn : DeciderInputBase>(
        private val parsedPathVariable: DecisionVariable<TIn, List<String>>,
        private val step: Int
) : DecisionVariable<TIn, String> {
    override fun getFrom(context: DecisionContext<TIn>): String = context[parsedPathVariable].getOrElse(step) { "" }
    override fun getDependencies(): List<DecisionVariable<TIn, *>> = listOf(parsedPathVariable)
    override fun isMetaVariable(): Boolean = true
    override fun toString(): String = "step $step of $parsedPathVariable"
}

private const val parameterStepPattern = ":([^(]+)?(?:\\((.*)\\))?"
private val parameterStepRe = Regex("^$parameterStepPattern$")
private val parametrizedPathExpressionRe = Regex(".*(^|/)$parameterStepPattern(/|$).*")

/**
 * A set of [DecisionCondition]'s generated from a path expression/pattern.
 *
 * Can be applied as part of [Decider]-based router implementation for web applications - for request
 * routing on backend or client-side routing on static site/SPA.
 *
 * [PathExpression] supports named parameters and regular expressions for parameters:
 *
 * expression
 *
 * ```
 * /trilobites/:trilobiteId([0-9a-z]{4})
 * ```
 *
 * will match
 *
 * ```
 * /trilobites/1234
 * ```
 *
 * but will not match
 *
 * ```
 * /trilobites/1
 * /trilobites/1234/legs
 * /trilobites/:trilobiteId([0-9a-z]{4})
 * ```
 *
 * except
 *
 * ```
 * /trilobites/1234/legs
 * ```
 *
 * will be matched if [exact] is set to `false`
 *
 * @param TIn type of [Decider]'s input
 * @param pathVariable [DecisionVariable] containing path to be matched to the expression
 * @param source pattern/expression source
 */
class PathExpression<TIn : DeciderInputBase>(
        private val pathVariable: DecisionVariable<TIn, String>,
        private val source: String,
        private val exact: Boolean = true
) {
    private val parsedPathVariable = ParsedPathVariable(pathVariable)
    private val _conditions: MutableList<DecisionCondition<TIn, *>> = mutableListOf()
    private val parameterMappings: MutableList<Pair<Int, String>> = mutableListOf()

    /**
     * Set of [DecisionCondition]'s that match when path matches the expression.
     */
    val conditions: Iterable<DecisionCondition<TIn, *>>
        get() = _conditions

    init {
        val exprSteps = parsePath(source)

        if (source.matches(parametrizedPathExpressionRe)) {
            exprSteps.forEachIndexed { step, stepExpr ->
                parameterStepRe.matchEntire(stepExpr)?.let { match ->
                    match.groups[2]?.value?.let { parameterReSource ->
                        _conditions += DecisionCondition(
                                RegexpMatchVariable(
                                        PathStepVariable(parsedPathVariable, step),
                                        parameterReSource
                                ),
                                true
                        )
                    }

                    match.groups[1]?.value?.let { parameterName ->
                        parameterMappings += step to parameterName
                    }

                    return@forEachIndexed
                }

                _conditions += DecisionCondition(
                        PathStepVariable(parsedPathVariable, step),
                        stepExpr
                )
            }

            if (exact) {
                _conditions += DecisionCondition(
                        CollectionSizeVariable(parsedPathVariable),
                        exprSteps.size
                )
            }
        } else {
            val canonicalPath = exprSteps.joinToString(separator = "/", prefix = "/")

            _conditions += if (exact) {
                DecisionCondition(
                        pathVariable,
                        canonicalPath
                )
            } else {
                DecisionCondition(
                        PredicateVariable(pathVariable) { it.startsWith(canonicalPath) },
                        true
                )
            }
        }
    }

    /**
     * Calls [callback] for each named parameter with values extracted from path stored in [context].
     */
    fun extractParameters(
            context: DecisionContext<TIn>,
            callback: (parameter: String, value: String) -> Unit
    ) {
        val parsedPath = context[parsedPathVariable]

        parameterMappings.forEach { (index, name) ->
            val value = parsedPath.getOrNull(index) ?: throw IllegalArgumentException(
                    "Path ${context[pathVariable]} doesn't match $this but is passed to it's #extractParameters()"
            )
            callback(name, value)
        }
    }

    override fun toString(): String = "${if (exact) "exact" else "inexact"} path expression \"$source\" for path from $pathVariable"
}
