package com.github.trilobiitti.trilobite.dna.data

import com.github.trilobiitti.trilobite.dna.testHelpers.DIAwareTest
import kotlin.test.*

class CollectionTypeTest : DIAwareTest {
    private val stringSet = CollectionType<String, Set<String>>(DataTypes.STRING, Set::class) {
        it.toSet()
    }

    @Test
    fun castShouldPassCollectionsWithValidElementTypeWithoutChangesCreationOfNewCollection() {
        val set = setOf<String>("foo", "bar", "baz")

        assertSame(set, stringSet.cast(set))
    }

    @Test
    fun castShouldCreateCollectionOfCollectionTypeIfElementTypesMatch() {
        val lst = listOf<String>("foo", "bar", "baz")

        assertEquals(lst.toSet(), stringSet.cast(lst))
    }

    @Test
    fun shouldNotAcceptCollectionWithElementsOfWrongType() {
        val lst = listOf("foo", 'b', 'a', 'r')

        assertFailsWith(ValidationError::class) {
            stringSet.cast(lst)
        }

        assertFalse(stringSet.isInstance(lst))
    }
}
