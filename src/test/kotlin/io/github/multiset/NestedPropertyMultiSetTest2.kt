package io.github.multiset

import io.github.multiset.NestedPropertyMultiSet.Companion.toMultiSet
import org.junit.Assert.*
import org.junit.Test
import kotlin.reflect.KProperty1

class NestedPropertyMultiSetTest2 {

    data class Dept(val id: String, val name: String)
    data class Address(val street: String, val city: String)
    data class Person(
        val name: String,
        val age: Int,
        val address: Address? = null,
        val department: NestedPropertyMultiSet<Dept>? = null,
    )

    @Test
    fun testEmptyMultiSet() {
        val emptyList = emptyList<Person>()
        val properties = listOf(Person::name, Person::age)
        val multiSet = emptyList.toMultiSet(properties)

        assertEquals(0, multiSet.elements.size)
    }

    @Test
    fun testSingleElementEquality() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Alice", 25)
        val properties = listOf(Person::name, Person::age)
        val multiSet1 = listOf(person1).toMultiSet(properties)
        val multiSet2 = listOf(person2).toMultiSet(properties)

        assertTrue(multiSet1.elementsEquals(multiSet2))
        assertEquals(multiSet1.contentHashCode(), multiSet2.contentHashCode())
    }

    @Test
    fun testDifferentElementsInequality() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Bob", 30)
        val properties = listOf(Person::name, Person::age)
        val multiSet1 = listOf(person1).toMultiSet(properties)
        val multiSet2 = listOf(person2).toMultiSet(properties)

        assertFalse(multiSet1.elementsEquals(multiSet2))
    }

    @Test
    fun testNestedMultiSetEquality() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("123 Main St", "Springfield")
        val person1 = Person("Alice", 25, address1)
        val person2 = Person("Alice", 25, address2)

        val addressProperties = listOf(Address::street, Address::city)
        val personProperties = listOf(Person::name, Person::age, Person::address)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        assertTrue(multiSet1.elementsEquals(multiSet2))
        assertEquals(multiSet1.contentHashCode(), multiSet2.contentHashCode())
    }

    @Test
    fun testNestedMultiSetInequality() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("456 Oak St", "Springfield")
        val person1 = Person("Alice", 25, address1)
        val person2 = Person("Alice", 25, address2)

        val addressProperties = listOf(Address::street, Address::city)
        val personProperties = listOf(Person::name, Person::age, Person::address)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        assertFalse(multiSet1.elementsEquals(multiSet2))
    }

    @Test
    fun testMultipleElementsEquality() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Bob", 30)
        val person3 = Person("Alice", 25)
        val person4 = Person("Bob", 30)

        val properties = listOf(Person::name, Person::age)
        val multiSet1 = listOf(person1, person2).toMultiSet(properties)
        val multiSet2 = listOf(person3, person4).toMultiSet(properties)

        assertTrue(multiSet1.elementsEquals(multiSet2))
        assertEquals(multiSet1.contentHashCode(), multiSet2.contentHashCode())
    }

    @Test
    fun testNullPropertyHandling() {
        val person1 = Person("Alice", 25, null)
        val person2 = Person("Alice", 25, null)
        val person3 = Person("Alice", 25, Address("123 Main St", "Springfield"))

        val properties = listOf(Person::name, Person::age, Person::address)
        val multiSet1 = listOf(person1).toMultiSet(properties)
        val multiSet2 = listOf(person2).toMultiSet(properties)
        val multiSet3 = listOf(person3).toMultiSet(properties)

        assertTrue(multiSet1.elementsEquals(multiSet2))
        assertFalse(multiSet1.elementsEquals(multiSet3))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptyPropertiesThrowsException() {
        val person = Person("Alice", 25)
        listOf(person).toMultiSet(emptyList())
    }

    @Test
    fun testNestedMultiSetHashConsistency() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("123 Main St", "Springfield")
        val person1 = Person("Alice", 25, address1)
        val person2 = Person("Alice", 25, address2)

        val addressProperties = listOf(Address::street, Address::city)
        val personProperties = listOf(Person::name, Person::age, Person::address)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        assertEquals(multiSet1.contentHashCode(), multiSet2.contentHashCode())
        assertTrue(multiSet1.elementsEquals(multiSet2))
    }

    @Test
    fun testDeepNestedMultiSetEquality() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("123 Main St", "Springfield")
        val person1 = Person("Alice", 25, address1)
        val person2 = Person("Alice", 25, address2)

        val addressProperties = listOf(Address::street, Address::city)
        val personProperties = listOf(Person::name, Person::age, Person::address)
        val outerMultiSet1 = listOf(person1).toMultiSet(personProperties)
        val outerMultiSet2 = listOf(person2).toMultiSet(personProperties)

        val outerProperties =
            listOf<KProperty1<NestedPropertyMultiSet<Person>, *>>(NestedPropertyMultiSet<Person>::elements)
        val doubleNestedMultiSet1 = listOf(outerMultiSet1).toMultiSet(outerProperties)
        val doubleNestedMultiSet2 = listOf(outerMultiSet2).toMultiSet(outerProperties)

        assertTrue(doubleNestedMultiSet1.elementsEquals(doubleNestedMultiSet2))
        assertEquals(doubleNestedMultiSet1.contentHashCode(), doubleNestedMultiSet2.contentHashCode())
    }

    @Test
    fun testIntersectionWithDifferentTypeNestedMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val dept3 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptMultiSet1 = listOf(dept1, dept2).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept3).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, department = deptMultiSet1)
        val person2 = Person("Bob", 30, department = deptMultiSet2)
        val person3 = Person("Alice", 25, department = deptMultiSet2)

        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person1, person2, person3).toMultiSet(personProperties)

        val result = multiSet1.intersect(multiSet2)

        assertEquals(1, result.size)
        assertEquals(person1, result[0])
    }

    @Test
    fun testDifferenceWithDifferentTypeNestedMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val dept3 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptMultiSet1 = listOf(dept1, dept2).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept3).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, department = deptMultiSet1)
        val person2 = Person("Bob", 30, department = deptMultiSet2)
        val person3 = Person("Charlie", 35, department = deptMultiSet1)

        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1, person2, person3).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        val result = multiSet1.difference(multiSet2)

        assertEquals(2, result.size)
        assertEquals(person1, result[0])
        assertEquals(person3, result[1])
    }

    @Test
    fun testIntersectionWithMultipleNestedTypes() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("123 Main St", "Springfield")
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D1", "Engineering")
        val addressProperties = listOf(Address::street, Address::city)
        val deptProperties = listOf(Dept::id, Dept::name)

        val addressMultiSet1 = listOf(address1).toMultiSet(addressProperties)
        val addressMultiSet2 = listOf(address2).toMultiSet(addressProperties)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept2).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, address1, deptMultiSet1)
        val person2 = Person("Bob", 30, address2, deptMultiSet2)
        val person3 = Person("Alice", 25, address2, deptMultiSet2)

        val personProperties = listOf(Person::name, Person::age, Person::address, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2, person3).toMultiSet(personProperties)

        val result = multiSet1.intersect(multiSet2)

        assertEquals(1, result.size)
        assertEquals(person1, result[0])
    }

    @Test
    fun testDifferenceWithMultipleNestedTypes() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("456 Oak St", "Springfield")
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val addressProperties = listOf(Address::street, Address::city)
        val deptProperties = listOf(Dept::id, Dept::name)

        val addressMultiSet1 = listOf(address1).toMultiSet(addressProperties)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept2).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, address1, deptMultiSet1)
        val person2 = Person("Bob", 30, address2, deptMultiSet2)
        val person3 = Person("Charlie", 35, address1, deptMultiSet1)

        val personProperties = listOf(Person::name, Person::age, Person::address, Person::department)
        val multiSet1 = listOf(person1, person3).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        val result = multiSet1.difference(multiSet2)

        assertEquals(2, result.size)
        assertTrue(result.contains(person1))
        assertTrue(result.contains(person3))
    }

    @Test
    fun testMatchElementsWithDifferentTypeNestedMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val dept3 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptMultiSet1 = listOf(dept1, dept2).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept3).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, department = deptMultiSet1)
        val person2 = Person("Bob", 30, department = deptMultiSet2)
        val person3 = Person("Alice", 25, department = deptMultiSet2)

        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2, person1, person3).toMultiSet(personProperties)

        val result = multiSet1.matchElements(multiSet2)

        assertEquals(1, result.size)
        assertEquals(listOf(person1), result[person1])
    }

    @Test
    fun testMatchElementsWithMultipleNestedTypes() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("123 Main St", "Springfield")
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D1", "Engineering")
        val addressProperties = listOf(Address::street, Address::city)
        val deptProperties = listOf(Dept::id, Dept::name)

        val addressMultiSet1 = listOf(address1).toMultiSet(addressProperties)
        val addressMultiSet2 = listOf(address2).toMultiSet(addressProperties)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept2).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, address1, deptMultiSet1)
        val person2 = Person("Bob", 30, address2, deptMultiSet2)
        val person3 = Person("Alice", 25, address2, deptMultiSet2)

        val personProperties = listOf(Person::name, Person::age, Person::address, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2, person3).toMultiSet(personProperties)

        val result = multiSet1.matchElements(multiSet2)

        assertEquals(1, result.size)
        assertEquals(listOf(person3), result[person1])
    }

    @Test
    fun testMatchElementsWithNoMatches() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("456 Oak St", "Springfield")
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val addressProperties = listOf(Address::street, Address::city)
        val deptProperties = listOf(Dept::id, Dept::name)

        val addressMultiSet1 = listOf(address1).toMultiSet(addressProperties)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept2).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, address1, deptMultiSet1)
        val person2 = Person("Bob", 30, address2, deptMultiSet2)

        val personProperties = listOf(Person::name, Person::age, Person::address, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        val result = multiSet1.matchElements(multiSet2)

        assertTrue(result.isEmpty())
    }

    @Test
    fun testMatchElementsWithEmptyMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, department = deptMultiSet1)
        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = emptyList<Person>().toMultiSet(personProperties)

        val result = multiSet1.matchElements(multiSet2)

        assertTrue(result.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testMatchElementsWithDifferentProperties() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Alice", 25)

        val multiSet1 = listOf(person1).toMultiSet(listOf(Person::name))
        val multiSet2 = listOf(person2).toMultiSet(listOf(Person::age))

        multiSet1.matchElements(multiSet2)
    }

    @Test
    fun testSymmetricDifferenceWithDifferentTypeNestedMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val dept3 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptMultiSet1 = listOf(dept1, dept2).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept3).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, department = deptMultiSet1)
        val person2 = Person("Bob", 30, department = deptMultiSet2)
        val person3 = Person("Alice", 25, department = deptMultiSet2)
        val person4 = Person("Charlie", 35, department = deptMultiSet1)

        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1, person2).toMultiSet(personProperties)
        val multiSet2 = listOf(person2, person3, person4).toMultiSet(personProperties)

        val (firstOnly, secondOnly) = multiSet1.symmetricDifference(multiSet2)

        assertEquals(listOf(person1), firstOnly)
        assertEquals(listOf(person3, person4), secondOnly)
    }

    @Test
    fun testSymmetricDifferenceWithMultipleNestedTypes() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("456 Oak St", "Springfield")
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val addressProperties = listOf(Address::street, Address::city)
        val deptProperties = listOf(Dept::id, Dept::name)

        val addressMultiSet1 = listOf(address1).toMultiSet(addressProperties)
        val addressMultiSet2 = listOf(address2).toMultiSet(addressProperties)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept2).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, address1, deptMultiSet1)
        val person2 = Person("Bob", 30, address2, deptMultiSet2)
        val person3 = Person("Charlie", 35, address1, deptMultiSet1)

        val personProperties = listOf(Person::name, Person::age, Person::address, Person::department)
        val multiSet1 = listOf(person1, person3).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        val (firstOnly, secondOnly) = multiSet1.symmetricDifference(multiSet2)

        assertEquals(listOf(person1, person3), firstOnly)
        assertEquals(listOf(person2), secondOnly)
    }

    @Test
    fun testSymmetricDifferenceWithEmptyMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, department = deptMultiSet1)
        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = emptyList<Person>().toMultiSet(personProperties)

        val (firstOnly, secondOnly) = multiSet1.symmetricDifference(multiSet2)

        assertEquals(listOf(person1), firstOnly)
        assertTrue(secondOnly.isEmpty())
    }

    @Test
    fun testSymmetricDifferenceWithIdenticalMultiSets() {
        val address1 = Address("123 Main St", "Springfield")
        val dept1 = Dept("D1", "Engineering")
        val addressProperties = listOf(Address::street, Address::city)
        val deptProperties = listOf(Dept::id, Dept::name)

        val addressMultiSet1 = listOf(address1).toMultiSet(addressProperties)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, address1, deptMultiSet1)
        val person2 = Person("Alice", 25, address1, deptMultiSet1)

        val personProperties = listOf(Person::name, Person::age, Person::address, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        val (firstOnly, secondOnly) = multiSet1.symmetricDifference(multiSet2)

        assertTrue(firstOnly.isEmpty())
        assertTrue(secondOnly.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSymmetricDifferenceWithDifferentProperties() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Alice", 25)

        val multiSet1 = listOf(person1).toMultiSet(listOf(Person::name))
        val multiSet2 = listOf(person2).toMultiSet(listOf(Person::age))

        multiSet1.symmetricDifference(multiSet2)
    }

    @Test
    fun testDifferenceWithLoggingSimpleCase() {
        val person1 = Person("Alice", 25)
        val person2 = Person("Bob", 30)
        val person3 = Person("Charlie", 35)

        val properties = listOf(Person::name, Person::age)
        val multiSet1 = listOf(person1, person2, person3).toMultiSet(properties)
        val multiSet2 = listOf(person1).toMultiSet(properties)

        val logs = mutableListOf<String>()
        val result = multiSet1.difference(multiSet2) { logs.add(it) }

        assertEquals(listOf(person2, person3), result)
        assertEquals(2, logs.size)
        assertTrue(logs.any { it.contains("No match for $person2 in other multiset, differences with $person1: name: Bob != Alice, age: 30 != 25") })
        assertTrue(logs.any { it.contains("No match for $person3 in other multiset, differences with $person1: name: Charlie != Alice, age: 35 != 25") })
    }

    @Test
    fun testDifferenceWithLoggingWithDifferentTypeNestedMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val dept3 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptMultiSet1 = listOf(dept1, dept2).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept3).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, department = deptMultiSet1)
        val person2 = Person("Bob", 30, department = deptMultiSet2)
        val person3 = Person("Charlie", 35, department = deptMultiSet1)

        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1, person3).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        val logs = mutableListOf<String>()
        val result = multiSet1.difference(multiSet2) { logs.add(it) }

        assertEquals(listOf(person1, person3), result) 
        assertEquals(2, logs.size)
        assertTrue(logs.any { it.contains("No match for $person1 in other multiset, differences with $person2: name: Alice != Bob, age: 25 != 30, department: $deptMultiSet1 != $deptMultiSet2") })
    }

    @Test
    fun testDifferenceWithLoggingWithMultipleNestedTypes() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("456 Oak St", "Springfield")
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val addressProperties = listOf(Address::street, Address::city)
        val deptProperties = listOf(Dept::id, Dept::name)

        val addressMultiSet1 = listOf(address1).toMultiSet(addressProperties)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)
        val deptMultiSet2 = listOf(dept2).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, address1, deptMultiSet1)
        val person2 = Person("Bob", 30, address2, deptMultiSet2)
        val person3 = Person("Charlie", 35, address1, deptMultiSet1)

        val personProperties = listOf(Person::name, Person::age, Person::address, Person::department)
        val multiSet1 = listOf(person1, person3).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        val logs = mutableListOf<String>()
        val result = multiSet1.difference(multiSet2) { logs.add(it) }

        assertEquals(listOf(person1, person3), result) 
        assertEquals(2, logs.size)
        assertTrue(logs.any { it.contains("No match for $person1 in other multiset, differences with $person2: name: Alice != Bob, age: 25 != 30, address: $address1 != $address2, department: $deptMultiSet1 != $deptMultiSet2") })
        assertTrue(logs.any { it.contains("No match for $person3 in other multiset, differences with $person2: name: Charlie != Bob, age: 35 != 30, address: $address1 != $address2, department: $deptMultiSet1 != $deptMultiSet2") })
    }

    @Test
    fun testDifferenceWithLoggingWithEmptyMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptMultiSet1 = listOf(dept1).toMultiSet(deptProperties)

        val person1 = Person("Alice", 25, department = deptMultiSet1)
        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = emptyList<Person>().toMultiSet(personProperties)

        val logs = mutableListOf<String>()
        val result = multiSet1.difference(multiSet2) { logs.add(it) }

        assertEquals(listOf(person1), result)
        assertTrue(logs.isEmpty()) // No comparisons made since other multiset is empty
    }

}