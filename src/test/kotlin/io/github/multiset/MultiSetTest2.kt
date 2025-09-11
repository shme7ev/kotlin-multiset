package io.github.multiset

import io.github.multiset.PropertyBasedMultiSet.Companion.toMultiSet
import org.junit.Assert.*
import org.junit.Test

class MultiSetTest2 {
    data class Department(val name: String, val code: Int)
    data class Address1(val street: String, val city: String)
    data class Person1(
        val name: String,
        val age: Int,
        val address: Address1? = null,
        val department: Department? = null,
    )

    @Test
    fun testNestedMultiSetWithDifferentTypesInequality() {
        val address1 = Address1("123 Main St", "Springfield")
        val dept1 = Department("Engineering", 101)
        val person1 = Person1("Alice", 25, address1)
        val person2 = Person1("Alice", 25, department = dept1)

        val personProperties1 = listOf(Person1::name, Person1::age, Person1::address)
        val personProperties2 = listOf(Person1::name, Person1::age, Person1::department)

        val multiSet1 = listOf(person1).toMultiSet(personProperties1)
        val multiSet2 = listOf(person2).toMultiSet(personProperties2)

        assertFalse(multiSet1.elementsEquals(multiSet2))
    }

    @Test
    fun testDeepNestedMultiSetWithMultipleTypes() {
        val address1 = Address1("123 Main St", "Springfield")
        val address2 = Address1("123 Main St", "Springfield")
        val dept1 = Department("Engineering", 101)
        val dept2 = Department("Engineering", 101)
        val person1 = Person1("Alice", 25, address1, dept1)
        val person2 = Person1("Alice", 25, address2, dept2)

        val personProperties = listOf(Person1::name, Person1::age, Person1::address, Person1::department)

        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        assertTrue(multiSet1.elementsEquals(multiSet2))
        assertEquals(multiSet1.contentHashCode(), multiSet2.contentHashCode())
    }

    @Test
    fun testNestedMultiSetWithNestedProperties() {
        val address1 = Address1("123 Main St", "Springfield")
        val address2 = Address1("123 Main St", "Springfield")
        val person1 = Person1("Alice", 25, address1)
        val person2 = Person1("Alice", 25, address2)

        val addressProperties1 = listOf(Address1::street, Address1::city)
        val personProperties = listOf(Person1::name, Person1::age, Person1::address)


        // Override the nested multisets with different properties
        val nestedMultiSet1 = listOf(address1).toMultiSet(addressProperties1)
        val nestedMultiSet2 = listOf(address2).toMultiSet(addressProperties1)

        val modifiedPerson1 = person1.copy(address = nestedMultiSet1.elements[0])
        val modifiedPerson2 = person2.copy(address = nestedMultiSet2.elements[0])

        val finalMultiSet1 = listOf(modifiedPerson1).toMultiSet(personProperties)
        val finalMultiSet2 = listOf(modifiedPerson2).toMultiSet(personProperties)

        assertTrue(finalMultiSet1.elementsEquals(finalMultiSet2))
    }

    @Test
    fun testNestedMultiSetWithNullFields() {
        val person1 = Person1("Alice", 25, null, null)
        val person2 = Person1("Alice", 25, null, null)
        val person3 = Person1("Alice", 25, Address1("123 Main St", "Springfield"), null)

        val personProperties = listOf(Person1::name, Person1::age, Person1::address, Person1::department)

        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)
        val multiSet3 = listOf(person3).toMultiSet(personProperties)

        assertTrue(multiSet1.elementsEquals(multiSet2))
        assertEquals(multiSet1.contentHashCode(), multiSet2.contentHashCode())
        assertFalse(multiSet1.elementsEquals(multiSet3))
    }

    @Test
    fun testNestedMultiSetWithEmptyNestedSet() {
        val person1 = Person1("Alice", 25, department = Department("Engineering", 101))
        val person2 = Person1("Alice", 25, department = Department("Engineering", 101))

        val deptProperties = listOf(Department::name, Department::code)
        val emptyDeptMultiSet = emptyList<Department>().toMultiSet(deptProperties)

        val personProperties = listOf(Person1::name, Person1::age, Person1::department)

        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 =
            listOf(person2.copy(department = emptyDeptMultiSet.elements.firstOrNull())).toMultiSet(personProperties)

        assertFalse(multiSet1.elementsEquals(multiSet2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptyPropertiesThrowsException() {
        val person = Person1("Alice", 25)
        listOf(person).toMultiSet(emptyList())
    }
}