package io.github.multiset

import io.github.multiset.PropertyBasedMultiSet.Companion.toMultiSet
import org.junit.Assert.*
import org.junit.Test
import kotlin.reflect.KProperty1

class HashCollisionTest {

    /**
     * A class that deliberately creates hash collisions for testing purposes.
     * All instances will have the same hashCode but may not be equal.
     */
    data class CollisionTest(val id: Int, val value: String) {
        override fun hashCode(): Int = 1  // Deliberately cause hash collisions

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CollisionTest) return false
            return id == other.id && value == other.value
        }
    }

    private val collisionTestId: KProperty1<CollisionTest, Int> = CollisionTest::id
    private val collisionTestValue: KProperty1<CollisionTest, String> = CollisionTest::value

    @Test
    fun testIntersectWithHashCollisions() {
        // Create elements that will have hash collisions
        val element1 = CollisionTest(1, "A")
        val element2 = CollisionTest(2, "B")
        val element3 = CollisionTest(1, "A") // Same as element1
        val element4 = CollisionTest(3, "C")
        val element5 = CollisionTest(2, "B") // Same as element2

        val listA = listOf(element1, element2, element4)
        val listB = listOf(element3, element5)

        val setA = listA.toMultiSet(listOf(collisionTestId, collisionTestValue))
        val setB = listB.toMultiSet(listOf(collisionTestId, collisionTestValue))

        val result = setA.intersect(setB)

        assertEquals(2, result.size)
        assertTrue(result.contains(element1))
        assertTrue(result.contains(element2))
        assertFalse(result.contains(element4))
    }

    @Test
    fun testDifferenceWithHashCollisions() {
        // Create elements that will have hash collisions
        val element1 = CollisionTest(1, "A")
        val element2 = CollisionTest(2, "B")
        val element3 = CollisionTest(1, "A") // Same as element1
        val element4 = CollisionTest(3, "C")
        val element5 = CollisionTest(2, "D") // Different value from element2

        val listA = listOf(element1, element2, element4)
        val listB = listOf(element3, element5)

        val setA = listA.toMultiSet(listOf(collisionTestId, collisionTestValue))
        val setB = listB.toMultiSet(listOf(collisionTestId, collisionTestValue))

        val result = setA.difference(setB)

        assertEquals(2, result.size)
        assertTrue(result.contains(element2)) // element2 is not in setB (element5 has different value)
        assertTrue(result.contains(element4)) // element4 is not in setB at all
        assertFalse(result.contains(element1)) // element1 is in both sets
    }

    @Test
    fun testSymmetricDifferenceWithHashCollisions() {
        // Create elements that will have hash collisions
        val element1 = CollisionTest(1, "A")
        val element2 = CollisionTest(2, "B")
        val element3 = CollisionTest(1, "A") // Same as element1
        val element4 = CollisionTest(3, "C")
        val element5 = CollisionTest(2, "D") // Different value from element2
        val element6 = CollisionTest(4, "E")

        val listA = listOf(element1, element2, element4)
        val listB = listOf(element3, element5, element6)

        val setA = listA.toMultiSet(listOf(collisionTestId, collisionTestValue))
        val setB = listB.toMultiSet(listOf(collisionTestId, collisionTestValue))

        val result = setA.symmetricDifference(setB)
        val firstOnly = result.first
        val secondOnly = result.second

        assertEquals(2, firstOnly.size)
        assertTrue(firstOnly.contains(element2)) // element2 is not in setB (element5 has different value)
        assertTrue(firstOnly.contains(element4)) // element4 is only in setA

        assertEquals(2, secondOnly.size)
        assertTrue(secondOnly.contains(element5)) // element5 is not in setA (element2 has different value)
        assertTrue(secondOnly.contains(element6)) // element6 is only in setB
    }

    @Test
    fun testCountWithHashCollisions() {
        // Create elements that will have hash collisions
        val element1 = CollisionTest(1, "A")
        val element2 = CollisionTest(1, "B")
        val element3 = CollisionTest(1, "A") // Same as element1
        val element4 = CollisionTest(1, "A") // Same as element1
        val element5 = CollisionTest(1, "B") // Same as element2

        val list = listOf(element1, element2, element3, element4, element5)

        val multiSet = list.toMultiSet(listOf(collisionTestId, collisionTestValue))

        assertEquals(3, multiSet.count(element1)) // element1 appears twice (element1, element3, element4)
        assertEquals(2, multiSet.count(element2)) // element2 appears twice (element2, element5)
        assertEquals(3, multiSet.count(element3)) // element3 is same as element1
        assertEquals(3, multiSet.count(element4)) // element4 is same as element1
        assertEquals(2, multiSet.count(element5)) // element5 is same as element2
    }

    @Test
    fun testMatchElementsWithHashCollisions() {
        // Create elements that will have hash collisions
        val element1 = CollisionTest(1, "A")
        val element2 = CollisionTest(2, "B")
        val element3 = CollisionTest(1, "A") // Same as element1
        val element4 = CollisionTest(1, "B") // Different from element1 and element2

        val listA = listOf(element1, element2)
        val listB = listOf(element3, element4)

        val setA = listA.toMultiSet(listOf(collisionTestId, collisionTestValue))
        val setB = listB.toMultiSet(listOf(collisionTestId, collisionTestValue))

        val result = setA.matchElements(setB)

        assertEquals(1, result.size)
        assertNotNull(result[element1])
        assertEquals(1, result[element1]?.size) // element1 matches element3
        assertTrue(result[element1]?.contains(element3) ?: false)
    }
}
