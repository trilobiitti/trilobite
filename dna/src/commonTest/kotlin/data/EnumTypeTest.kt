package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.testHelpers.DIAwareTest
import kotlin.test.*

class EnumTypeTest : DIAwareTest {
    private val theEnum = EnumType<String>(setOf("foo", "bar", "baz"), DataTypes.STRING)

    @Test
    fun shouldAcceptCorrectValues() {
        assertEquals("foo", theEnum.cast("foo"))
        assertTrue(theEnum.isInstance("baz"))
        theEnum.validate("bar")
    }

    @Test
    fun shouldNotAcceptValuesInvalidForBaseType() {
        assertFailsWith(ValidationError::class) {
            theEnum.cast(theEnum)
        }

        assertFalse(theEnum.isInstance(42))
    }

    @Test
    fun shouldNotAcceptValidValuesOfBaseTypeThatAreNotEnumValues() {
        assertFailsWith(ValidationError::class) {
            theEnum.cast("buzz")
        }

        assertFailsWith(ValidationError::class) {
            theEnum.validate("bus")
        }

        assertFalse(theEnum.isInstance("Bar"))
    }
}
