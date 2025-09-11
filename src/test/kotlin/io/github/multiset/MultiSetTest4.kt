package io.github.multiset

import io.github.multiset.PropertyBasedMultiSet.Companion.toMultiSet
import org.junit.Assert.*
import org.junit.Test

class MultiSetTest4 {

    data class Department(val name: String, val code: Int)
    data class Address(val street: String, val city: String)
    data class Person(val name: String, val age: Int, val address: Address? = null, val department: Department? = null)

    private val properties = listOf(Person::name, Person::age)

    @Test
    fun testIntersectWithCommonElements() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Bob", 30)
        val person3 = Person("Alice", 25)

        val multiSet1 = PropertyBasedMultiSet(listOf(person1, person2), properties)
        val multiSet2 = PropertyBasedMultiSet(listOf(person2, person3), properties)

        val result = multiSet1.intersect(multiSet2)

        assertEquals(2, result.size)
        assertTrue(result.contains(person1))
        assertTrue(result.contains(person2))
    }

    @Test
    fun testIntersectWithNoCommonElements() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Bob", 30)
        val person3 = Person("Charlie", 35)

        val multiSet1 = PropertyBasedMultiSet(listOf(person1, person2), properties)
        val multiSet2 = PropertyBasedMultiSet(listOf(person3), properties)

        val result = multiSet1.intersect(multiSet2)

        assertTrue(result.isEmpty())
    }

    @Test
    fun testIntersectWithEmptySet() {
        val person1 = Person("Alice", 25)
        val multiSet1 = PropertyBasedMultiSet(listOf(person1), properties)
        val multiSet2 = PropertyBasedMultiSet(emptyList(), properties)

        val result = multiSet1.intersect(multiSet2)

        assertTrue(result.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIntersectWithDifferentProperties() {
        val multiSet1 = PropertyBasedMultiSet(listOf(Person("Alice", 25)), listOf(Person::name))
        val multiSet2 = PropertyBasedMultiSet(listOf(Person("Alice", 25)), listOf(Person::age))

        multiSet1.intersect(multiSet2)
    }

    @Test
    fun testDifferenceWithCommonElements() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Bob", 30)
        val person3 = Person("Charlie", 35)

        val multiSet1 = PropertyBasedMultiSet(listOf(person1, person2, person3), properties)
        val multiSet2 = PropertyBasedMultiSet(listOf(person1, person2), properties)

        val result = multiSet1.difference(multiSet2)

        assertEquals(1, result.size)
        assertEquals(person3, result[0])
    }

    @Test
    fun testDifferenceWithNoCommonElements() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Bob", 30)
        val person3 = Person("Charlie", 35)

        val multiSet1 = PropertyBasedMultiSet(listOf(person1, person2), properties)
        val multiSet2 = PropertyBasedMultiSet(listOf(person3), properties)

        val result = multiSet1.difference(multiSet2)

        assertEquals(2, result.size)
        assertTrue(result.contains(person1))
        assertTrue(result.contains(person2))
    }

    @Test
    fun testDifferenceWithEmptySet() {
        val person1 = Person("Alice", 25)
        val multiSet1 = PropertyBasedMultiSet(listOf(person1), properties)
        val multiSet2 = PropertyBasedMultiSet(emptyList<Person>(), properties)

        val result = multiSet1.difference(multiSet2)

        assertEquals(1, result.size)
        assertEquals(person1, result[0])
    }

    @Test(expected = IllegalArgumentException::class)
    fun testDifferenceWithDifferentProperties() {
        val multiSet1 = PropertyBasedMultiSet(listOf(Person("Alice", 25)), listOf(Person::name))
        val multiSet2 = PropertyBasedMultiSet(listOf(Person("Alice", 25)), listOf(Person::age))

        multiSet1.difference(multiSet2)
    }

    @Test
    fun testContainsExistingElement() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Alice", 25)
        val multiSet = PropertyBasedMultiSet(listOf(person1), properties)

        assertTrue(multiSet.contains(person2))
    }

    @Test
    fun testContainsNonExistingElement() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Bob", 30)
        val multiSet = PropertyBasedMultiSet(listOf(person1), properties)

        assertFalse(multiSet.contains(person2))
    }

    @Test
    fun testCountWithMultipleOccurrences() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Alice", 25)
        val person3 = Person("Bob", 30)
        val multiSet = PropertyBasedMultiSet(listOf(person1, person2, person3), properties)

        assertEquals(2, multiSet.count(Person("Alice", 25)))
        assertEquals(1, multiSet.count(Person("Bob", 30)))
        assertEquals(0, multiSet.count(Person("Charlie", 35)))
    }

    @Test
    fun testCountWithEmptySet() {
        val multiSet = PropertyBasedMultiSet(emptyList(), properties)

        assertEquals(0, multiSet.count(Person("Alice", 25)))
    }

    @Test
    fun testNestedMultiSetIntersection() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("456 Oak St", "Springfield")
        val person1 = Person("Alice", 25, address1)
        val person2 = Person("Bob", 30, address2)
        val person3 = Person("Alice", 25, address1)

        val addressProperties = listOf(Address::street, Address::city)
        val personProperties = listOf(Person::name, Person::age, Person::address)

        val multiSet1 = PropertyBasedMultiSet(listOf(person1, person2), personProperties, mapOf(Person::address to addressProperties ))
        val multiSet2 = PropertyBasedMultiSet(listOf(person2, person3), personProperties, mapOf(Person::address to addressProperties ))

        val result = multiSet1.intersect(multiSet2)

        assertEquals(2, result.size)
        assertTrue(result.contains(person1))
        assertTrue(result.contains(person2))
    }

    @Test
    fun testNestedMultiSetDifference() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("456 Oak St", "Springfield")
        val person1 = Person("Alice", 25, address1)
        val person2 = Person("Bob", 30, address2)
        val person3 = Person("Charlie", 35, address1)

        val addressProperties = listOf(Address::street, Address::city)

        val personProperties = listOf(Person::name, Person::age, Person::address)

        val multiSet1 = listOf(person1, person2, person3).toMultiSet( personProperties, mapOf(Person::address to addressProperties ))
        val multiSet2 = listOf(person1, person2).toMultiSet( personProperties, mapOf(Person::address to addressProperties ))

        val result = multiSet1.difference(multiSet2)

        assertEquals(1, result.size)
        assertEquals(person3, result[0])
    }
}
