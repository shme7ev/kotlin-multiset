package io.github.multiset

import io.github.multiset.PropertyBasedMultiSet.Companion.toMultiSet
import org.junit.Assert.*
import org.junit.Test
import kotlin.reflect.KProperty1

class MultiSetTest3 {

    data class Dept(val id: String, val name: String)
    data class Address(val street: String, val city: String)
    data class Person(
        val name: String,
        val age: Int,
        val address: Address? = null,
        val department: List<Dept>? = null,
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
        val multiSet1 = listOf(person1).toMultiSet(personProperties, mapOf(Person::address to addressProperties))
        val multiSet2 = listOf(person2).toMultiSet(personProperties, mapOf(Person::address to addressProperties))

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
        val multiSet1 = listOf(person1).toMultiSet(personProperties, mapOf(Person::address to addressProperties))
        val multiSet2 = listOf(person2).toMultiSet(personProperties, mapOf(Person::address to addressProperties))

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
    fun testNestedMultiSetHashConsistency() {
        val address1 = Address("123 Main St", "Springfield")
        val address2 = Address("123 Main St", "Springfield")
        val person1 = Person("Alice", 25, address1)
        val person2 = Person("Alice", 25, address2)

        val personProperties = listOf(Person::name, Person::age, Person::address)
        val multiSet1 = listOf(person1).toMultiSet(personProperties)
        val multiSet2 = listOf(person2).toMultiSet(personProperties)

        assertEquals(multiSet1.contentHashCode(), multiSet2.contentHashCode())
        assertTrue(multiSet1.elementsEquals(multiSet2))
    }

    @Test
    fun testIntersectionWithDifferentTypeNestedMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val dept3 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptList1 = listOf(dept1, dept2)
        val deptList2 = listOf(dept3)

        val person1 = Person("Alice", 25, department = deptList1)
        val person2 = Person("Bob", 30, department = deptList2)
        val person3 = Person("Alice", 25, department = deptList2)

        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties, mapOf(Person::department to deptProperties))
        val multiSet2 =
            listOf(person1, person2, person3).toMultiSet(personProperties, mapOf(Person::department to deptProperties))

        val result = multiSet1.intersect(multiSet2)

        assertEquals(1, result.size)
        assertEquals(person1, result[0])
    }

    @Test
    fun testDifferenceWithDifferentTypeNestedMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val dept3 = Dept("D1", "Engineering")
        val deptMultiSet1 = listOf(dept1, dept2)
        val deptMultiSet2 = listOf(dept3)

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

        val deptMultiSet1 = listOf(dept1)
        val deptMultiSet2 = listOf(dept2)

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

        val deptMultiSet1 = listOf(dept1)
        val deptMultiSet2 = listOf(dept2)

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
        val deptMultiSet1 = listOf(dept1, dept2)
        val deptMultiSet2 = listOf(dept3)

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

        val deptMultiSet1 = listOf(dept1)
        val deptMultiSet2 = listOf(dept2)

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
        val deptMultiSet1 = listOf(dept1)
        val deptMultiSet2 = listOf(dept2)

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
        val deptMultiSet1 = listOf(dept1)

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
        val deptMultiSet1 = listOf(dept1, dept2)
        val deptMultiSet2 = listOf(dept3)

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
        val deptMultiSet1 = listOf(dept1)
        val deptMultiSet2 = listOf(dept2)

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
        val deptMultiSet1 = listOf(dept1)

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
        val deptMultiSet1 = listOf(dept1)

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
    fun testDifferenceWithLoggingWithDifferentTypeNestedMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val dept2 = Dept("D2", "HR")
        val dept3 = Dept("D3", "Research")

        val deptProperties = listOf(Dept::id, Dept::name)
        val depts12 = listOf(dept1, dept2)
        val depts2 = listOf(dept2)
        val depts3 = listOf(dept3)

        val alice = Person("Alice", 25, department = depts12)
        val bob = Person("Bob", 30, department = depts2)
        val charlie = Person("Charlie", 35, department = depts3)

        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multisetProperties: Map<KProperty1<Person, *>, List<KProperty1<Dept, String>>> =
            mapOf(Person::department to deptProperties)
        val multiSet1 =
            listOf(alice, charlie).toMultiSet(personProperties, multisetProperties)
        val multiSet2 = listOf(bob).toMultiSet(personProperties, multisetProperties)

        val logs = mutableListOf<String>()
        val result = multiSet1.difference(multiSet2) { logs.add(it) }

        logs.forEach { println(it) }

        assertEquals(listOf(alice, charlie), result)
        assertEquals(2, logs.size)
        assertTrue(logs.any { it.contains("No match for $alice in other multiset, differences with $bob:") })
        assertTrue(logs.any { it.contains("name: Alice != Bob") })
        assertTrue(logs.any { it.contains("age: 25 != 30") })
        assertTrue(logs.any { it.contains("department: $depts12 != $depts2") })
        assertTrue(logs.any { it.contains("List sizes are different 2 1") })
        assertTrue(logs.any { it.contains("name: Charlie != Bob") })
        assertTrue(logs.any { it.contains("age: 35 != 30") })
        assertTrue(logs.any { it.contains("id: D3 != D2") })
        assertTrue(logs.any { it.contains("name: Research != HR") })
    }

    @Test
    fun testDifferenceWithLoggingWithEmptyMultiSet() {
        val dept1 = Dept("D1", "Engineering")
        val deptProperties = listOf(Dept::id, Dept::name)
        val deptMultiSet1 = listOf(dept1)

        val person1 = Person("Alice", 25, department = deptMultiSet1)
        val personProperties = listOf(Person::name, Person::age, Person::department)
        val multiSet1 = listOf(person1).toMultiSet(personProperties, mapOf(Person::department to deptProperties))
        val multiSet2 = emptyList<Person>().toMultiSet(personProperties)

        val logs = mutableListOf<String>()
        val result = multiSet1.difference(multiSet2) { logs.add(it) }

        assertEquals(listOf(person1), result)
        assertTrue(logs.isEmpty()) // No comparisons made since other multiset is empty
    }

}