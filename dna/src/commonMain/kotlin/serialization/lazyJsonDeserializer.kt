package com.github.trilobiitti.trilobite.dna.serialization

import com.github.trilobiitti.trilobite.dna.data.Document
import com.github.trilobiitti.trilobite.dna.data.DocumentFieldKey
import com.github.trilobiitti.trilobite.dna.data.DocumentVisitor
import com.github.trilobiitti.trilobite.dna.data.Documents

private const val TRUE_STR = "true"
private const val FALSE_STR = "false"
private const val NULL_STR = "null"

abstract class LazyJsonDeserializer<
    TObject : Any,
    TArray : Any,
    TObjectBuilder,
    TArrayBuilder
    > {
    abstract fun startObject(): TObjectBuilder
    abstract fun addField(builder: TObjectBuilder, fieldName: String, value: Any?): TObjectBuilder
    abstract fun endObject(builder: TObjectBuilder): TObject
    abstract fun lazyObject(string: String, spanStart: Int, spanEnd: Int): TObject

    abstract fun startArray(): TArrayBuilder
    abstract fun addElement(builder: TArrayBuilder, value: Any?): TArrayBuilder
    abstract fun endArray(builder: TArrayBuilder): TArray
    abstract fun lazyArray(string: String, spanStart: Int, spanEnd: Int): TArray

    open fun parseFloat(string: String, spanStart: Int, spanEnd: Int): Number =
        string
            .substring(spanStart, spanEnd)
            .toDouble()

    open fun parseInteger(string: String, spanStart: Int, spanEnd: Int): Number =
        parseFloat(string, spanStart, spanEnd)

    protected fun parseObject(string: String, spanStart: Int, spanEnd: Int): TObject {
        var index = skipWhitespace(string, spanStart + 1)
        var builder = startObject()

        if (string[index] == '}') {
            return endObject(builder)
        }

        while (true) {
            val (i0, key) = parseString(string, index)

            index = skipWhitespace(string, i0)

            when (val c = string[index]) {
                ':' -> {
                }
                else -> throw IllegalArgumentException(
                    "Unexpected character '$c' at position $index in JSON string. Expected is: ':'."
                )
            }

            index = skipWhitespace(string, index + 1)

            val (i1, value) = parseValueLazy(string, index)

            index = skipWhitespace(string, i1)
            builder = addField(builder, key, value)

            when (val c = string[index]) {
                ',' -> {
                    ++index
                }
                '}' -> return endObject(builder)
                else -> throw IllegalArgumentException(
                    "Unexpected character '$c' at position $index in JSON string. Expected are: ',', '}'."
                )
            }

            index = skipWhitespace(string, index)
        }
    }

    protected fun parseArray(string: String, spanStart: Int, spanEnd: Int): TArray {
        var index = skipWhitespace(string, spanStart + 1)
        var builder = startArray()

        if (string[index] == ']') {
            return endArray(builder)
        }

        while (true) {
            val (i, v) = parseValueLazy(string, index)

            builder = addElement(builder, v)
            index = skipWhitespace(string, i)

            when (val c = string[index]) {
                ',' -> {
                    ++index
                }
                ']' -> return endArray(builder)
                else -> throw IllegalArgumentException(
                    "Unexpected character '$c' at position $index in JSON string. Expected are: ',', ']'."
                )
            }

            index = skipWhitespace(string, index)
        }
    }

    /**
     * ```
     * {"answer": 42.0}
     *            ^------ start
     *                ^-- returned
     * ```
     */
    private fun parseNumber(string: String, start: Int): Pair<Int, Number> {
        var index = start + 1 // `parseNumber` is expected to be called with `string[start]` of '-' or '0'..'9'
        val len = string.length

        parseInt@ while (true) {
            if (index == len) {
                return index to parseInteger(string, start, index)
            }

            when (string[index]) {
                '.' -> break@parseInt
                'e' -> break@parseInt
                'E' -> break@parseInt
                in '0'..'9' -> {
                    ++index
                }
                else -> return index to parseInteger(string, start, index)
            }
        }
        // `string[index]` is ether '.' or 'e'/'E'
        if (string[index] == '.') {
            ++index

            parseFraction@ while (true) {
                if (index == len) {
                    return index to parseFloat(string, start, index)
                }

                when (string[index]) {
                    in '0'..'9' -> {
                        ++index
                    }
                    'e' -> break@parseFraction
                    'E' -> break@parseFraction
                    else -> return index to parseFloat(string, start, index)
                }
            }
        }

        // `string[index]` is 'e' or 'E'
        ++index

        when (val c = string[index]) {
            '+' -> {
                ++index
            }
            '-' -> {
                ++index
            }
            in '0'..'9' -> {
            }
            else -> throw IllegalArgumentException(
                "Illegal character '$c' at position $index in JSON string"
            )
        }

        if (string[index] !in '0'..'9') {
            throw IllegalArgumentException(
                "Invalid numeric literal at position $index. At least one exponent digit is expected after '${string[index - 1]}'"
            )
        }

        ++index

        while (true) {
            when {
                len == index -> break
                string[index] !in '0'..'9' -> break
            }

            ++index
        }

        return index to parseFloat(string, start, index)
    }

    /**
     * ```
     * ["Hello world!"]
     *  ^---------------- start
     *                ^-- end
     * ```
     */
    private fun parseString(string: String, start: Int): Pair<Int, String> {
        var index = start

        noEscapeString@ while (true) {
            ++index

            when (string[index]) {
                '"' -> {
                    return index + 1 to string.substring(start + 1, index)
                }
                '\\' -> {
                    break@noEscapeString
                }
            }
        }

        val stringBuilder = StringBuilder(string.subSequence(start + 1, index))

        while (true) {
            when (val c = string[index]) {
                '"' -> return index + 1 to stringBuilder.toString()
                '\\' -> {
                    ++index
                    stringBuilder.append(
                        when (val esc = string[index]) {
                            '"' -> '"'
                            '\\' -> '\\'
                            '/' -> '/'
                            'b' -> '\b'
                            'f' -> '\u000C'
                            'n' -> '\n'
                            'r' -> '\r'
                            't' -> '\t'
                            'u' -> {
                                index += 4
                                string.substring(index - 3, index + 1).toInt(16).toChar()
                            }
                            else -> throw IllegalArgumentException(
                                "Illegal string escape character '$esc' at position $index in JSON string"
                            )
                        }
                    )
                }
                else -> stringBuilder.append(c)
            }

            ++index
        }
    }

    /**
     * ```
     * [{"foo": "bar"}]
     *  ^---------------- start
     *                ^-- returned
     * ```
     *
     * ```
     * {"foo": ["bar"]}
     *         ^--------- start
     *                ^-- returned
     * ```
     */
    private fun skipStructure(string: String, start: Int, open: Char, close: Char): Int {
        var index = start
        var depth = 0

        do {
            when (string[index]) {
                open -> {
                    ++depth
                }
                close -> {
                    --depth
                }
                '"' -> {
                    skipString@ while (true) {
                        ++index

                        when (string[index]) {
                            '\\' -> {
                                ++index
                            }
                            '"' -> break@skipString
                        }
                    }
                }
            }

            ++index
        } while (depth != 0)

        return index
    }

    /**
     * ```
     * "foo",    42
     *       ^------- start
     *           ^--- returned
     * ```
     */
    private fun skipWhitespace(string: String, start: Int): Int {
        var index = start

        while (string[index].isWhitespace()) {
            ++index
        }

        return index
    }

    private fun parseValueLazy(string: String, spanStart: Int): Pair<Int, Any?> {
        when (string[spanStart]) {
            '{' -> {
                val end = skipStructure(string, spanStart, '{', '}')

                return end to lazyObject(string, spanStart, end)
            }

            '[' -> {
                val end = skipStructure(string, spanStart, '[', ']')

                return end to lazyArray(string, spanStart, end)
            }

            '"' -> {
                return parseString(string, spanStart)
            }

            't' -> {
                if (string.regionMatches(spanStart, TRUE_STR, 0, TRUE_STR.length, false)) {
                    return spanStart + TRUE_STR.length to true
                }
            }

            'f' -> {
                if (string.regionMatches(spanStart, FALSE_STR, 0, FALSE_STR.length, false)) {
                    return spanStart + FALSE_STR.length to false
                }
            }

            'n' -> {
                if (string.regionMatches(spanStart, NULL_STR, 0, NULL_STR.length, false)) {
                    return spanStart + NULL_STR.length to null
                }
            }

            '-' -> {
                return parseNumber(string, spanStart)
            }

            in '0'..'9' -> {
                return parseNumber(string, spanStart)
            }
        }

        throw IllegalArgumentException(
            "Illegal character '${string[spanStart]}' at position $spanStart in JSON string"
        )
    }

    fun parse(string: String): Any? = parseValueLazy(string, skipWhitespace(string, 0)).second
}

// // WTF-KOTLIN-JS: Fails with `TypeError: Cannot read property 'prototype' of undefined` from `kotlin.Number`
// // https://youtrack.jetbrains.com/issue/KT-17345
// class LazyNumber(
//        private val source: String
// ): Number() {
//    override fun toByte(): Byte = source.toByte()
//    override fun toChar(): Char = source.toInt().toChar()
//    override fun toDouble(): Double = source.toDouble()
//    override fun toFloat(): Float = source.toFloat()
//    override fun toInt(): Int = source.toInt()
//    override fun toLong(): Long = source.toLong()
//    override fun toShort(): Short = source.toShort()
//
//    override fun toString(): String = source
// }

open class ImmutablePrimitiveLazyJsonDeserializer : LazyJsonDeserializer<
    Map<String, Any?>,
    List<Any?>,
    MutableMap<String, Any?>?,
    MutableList<Any?>?
    >() {
    override fun startObject(): MutableMap<String, Any?>? = null

    override fun addField(builder: MutableMap<String, Any?>?, fieldName: String, value: Any?): MutableMap<String, Any?>? =
        builder?.also { it[fieldName] = value } ?: mutableMapOf(fieldName to value)

    override fun endObject(builder: MutableMap<String, Any?>?): Map<String, Any?> = builder ?: emptyMap()

    private inner class LazyMap(
        var string: String?,
        val spanStart: Int,
        val spanEnd: Int
    ) : Map<String, Any?> {
        val lm: Map<String, Any?> by lazy {
            val m = parseObject(string!!, spanStart, spanEnd)

            string = null

            return@lazy m
        }
        override val entries: Set<Map.Entry<String, Any?>>
            get() = lm.entries
        override val keys: Set<String>
            get() = lm.keys
        override val size: Int
            get() = lm.size
        override val values: Collection<Any?>
            get() = lm.values

        override fun containsKey(key: String): Boolean = lm.containsKey(key)
        override fun containsValue(value: Any?): Boolean = lm.containsValue(value)
        override fun get(key: String): Any? = lm.get(key)
        override fun isEmpty(): Boolean = lm.isEmpty()

        override fun equals(other: Any?): Boolean = lm == other
        override fun hashCode(): Int = lm.hashCode()

        override fun toString(): String = "LazyMap($lm)"
    }

    override fun lazyObject(string: String, spanStart: Int, spanEnd: Int): Map<String, Any?> = LazyMap(
        string, spanStart, spanEnd
    )

    override fun startArray(): MutableList<Any?>? = null

    override fun addElement(builder: MutableList<Any?>?, value: Any?): MutableList<Any?>? =
        builder?.also { it.add(value) } ?: mutableListOf(value)

    override fun endArray(builder: MutableList<Any?>?): List<Any?> = builder ?: emptyList()

    private inner class LazyList(
        var string: String?,
        val spanStart: Int,
        val spanEnd: Int
    ) : List<Any?> {
        val ll: List<Any?> by lazy {
            val m = parseArray(string!!, spanStart, spanEnd)

            string = null

            return@lazy m
        }
        override val size: Int
            get() = ll.size

        override fun contains(element: Any?): Boolean = ll.contains(element)
        override fun containsAll(elements: Collection<Any?>): Boolean = ll.containsAll(elements)
        override fun get(index: Int): Any? = ll.get(index)
        override fun indexOf(element: Any?): Int = ll.indexOf(element)
        override fun isEmpty(): Boolean = ll.isEmpty()
        override fun iterator(): Iterator<Any?> = ll.iterator()
        override fun lastIndexOf(element: Any?): Int = ll.lastIndexOf(element)
        override fun listIterator(): ListIterator<Any?> = ll.listIterator()
        override fun listIterator(index: Int): ListIterator<Any?> = ll.listIterator(index)
        override fun subList(fromIndex: Int, toIndex: Int): List<Any?> = ll.subList(fromIndex, toIndex)

        override fun equals(other: Any?): Boolean = ll == other
        override fun hashCode(): Int = ll.hashCode()

        override fun toString(): String = "LazyList($ll)"
    }

    override fun lazyArray(string: String, spanStart: Int, spanEnd: Int): List<Any?> = LazyList(
        string, spanStart, spanEnd
    )
}

open class LazyDocumentDeserializer : LazyJsonDeserializer<
    Document,
    MutableList<Any?>,
    Document,
    MutableList<Any?>
    >() {
    override fun startObject(): Document = Documents.newEmptyDocument()

    override fun addField(builder: Document, fieldName: String, value: Any?): Document = builder.also {
        it[DocumentFieldKey(fieldName)] = value
    }

    override fun endObject(builder: Document): Document = builder

    private inner class LazyDocument(
        private var string: String?,
        private val spanStart: Int,
        private val spanEnd: Int
    ) : Document {
        private val ld by lazy {
            val d = parseObject(string!!, spanStart, spanEnd)

            string = null

            return@lazy d
        }

        override fun get(key: DocumentFieldKey): Any? = ld[key]

        override fun visit(visitor: DocumentVisitor) = ld.visit(visitor)

        override fun set(key: DocumentFieldKey, value: Any?) {
            ld[key] = value
        }

        override fun toString(): String = "LazyDocument($ld)"
    }

    override fun lazyObject(string: String, spanStart: Int, spanEnd: Int): Document = LazyDocument(
        string, spanStart, spanEnd
    )

    override fun startArray(): MutableList<Any?> = mutableListOf()

    override fun addElement(builder: MutableList<Any?>, value: Any?): MutableList<Any?> = builder.apply { add(value) }

    override fun endArray(builder: MutableList<Any?>): MutableList<Any?> = builder

    private inner class LazyMutableList(
        var string: String?,
        val spanStart: Int,
        val spanEnd: Int
    ) : MutableList<Any?> {
        val ll by lazy {
            val l = parseArray(string!!, spanStart, spanEnd)

            string = null

            return@lazy l
        }

        override val size: Int
            get() = ll.size

        override fun contains(element: Any?): Boolean = ll.contains(element)
        override fun containsAll(elements: Collection<Any?>): Boolean = ll.containsAll(elements)
        override fun get(index: Int): Any? = ll.get(index)
        override fun indexOf(element: Any?): Int = ll.indexOf(element)
        override fun isEmpty(): Boolean = ll.isEmpty()
        override fun iterator(): MutableIterator<Any?> = ll.iterator()
        override fun lastIndexOf(element: Any?): Int = ll.lastIndexOf(element)
        override fun add(element: Any?): Boolean = ll.add(element)
        override fun add(index: Int, element: Any?) = ll.add(index, element)
        override fun addAll(index: Int, elements: Collection<Any?>): Boolean = ll.addAll(index, elements)
        override fun addAll(elements: Collection<Any?>): Boolean = ll.addAll(elements)
        override fun clear() = ll.clear()
        override fun listIterator(): MutableListIterator<Any?> = ll.listIterator()
        override fun listIterator(index: Int): MutableListIterator<Any?> = ll.listIterator(index)
        override fun remove(element: Any?): Boolean = ll.remove(element)
        override fun removeAll(elements: Collection<Any?>): Boolean = ll.removeAll(elements)
        override fun removeAt(index: Int): Any? = ll.removeAt(index)
        override fun retainAll(elements: Collection<Any?>): Boolean = ll.retainAll(elements)
        override fun set(index: Int, element: Any?): Any? = ll.set(index, element)
        override fun subList(fromIndex: Int, toIndex: Int): MutableList<Any?> = ll.subList(fromIndex, toIndex)
    }

    override fun lazyArray(string: String, spanStart: Int, spanEnd: Int): MutableList<Any?> = LazyMutableList(
        string, spanStart, spanEnd
    )
}
