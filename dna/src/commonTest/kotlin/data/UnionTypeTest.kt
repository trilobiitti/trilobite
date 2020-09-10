package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.testHelpers.DIAwareTest
import kotlin.test.*

class UnionTypeTest : DIAwareTest {
    private val floats = UnionType<Number>(listOf(DataTypes.FLOAT, DataTypes.DOUBLE), DataTypes.NUMBER)

    @Test
    fun shouldRecognizeValuesOfUnitedTypes() {
        assertEquals(42.0f, floats.cast(42.0f))
        assertEquals(42.0, floats.cast(42.0))

        assertTrue(floats.isInstance(42.0))
        assertTrue(floats.isInstance(42.0f))

        floats.validate(42.0)
        floats.validate(42.0f)
    }

    @Test
    fun shouldNotRecognizeValuesOfOtherTypes() {
        assertFailsWith(ValidationError::class) {
            floats.cast("42.0")
        }

        /* WTF-KOTLIN-JS-OF-THE-DAY: Just WTF?
         */
        if (!Double::class.isInstance(42)) {
            assertFailsWith(ValidationError::class) {
                floats.validate(42)
            }
        }

        assertFalse(floats.isInstance(42L))
    }
}
