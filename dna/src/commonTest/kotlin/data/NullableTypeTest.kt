package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.testHelpers.DIAwareTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NullableTypeTest : DIAwareTest {
    private val nullableString = NullableType(DataTypes.STRING)

    @Test
    fun shouldAcceptNullAsValidValue() {
        assertEquals(null, nullableString.cast(null))
        assertEquals(true, nullableString.isInstance(null))

        nullableString.validate(null)
    }

    @Test
    fun shouldAcceptNonNullValuesOfBaseTypeAsValidValues() {
        assertEquals("test", nullableString.cast("test"))
        assertEquals(true, nullableString.isInstance("test"))

        nullableString.validate("test")
    }

    @Test
    fun shouldNotAcceptInvalidValuesOfBaseType() {
        assertFailsWith(ValidationError::class) {
            nullableString.cast(42)
        }

        assertEquals(false, nullableString.isInstance(42))
    }
}
