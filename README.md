# kotlin-multiset

A Kotlin library for working with multisets (bags) that allows comparing complex objects based on their properties. 

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/shme7ev/kotlin-multiset)

## Features

- Compare complex objects based on selected properties
- Support for nested lists with their own comparison properties - currently one nested level only
- Operations like intersection, difference, symmetric difference
- Proper handling of BigDecimal comparisons
- Once-only hash calculation for multiset elements - effective for large multisets with many element properties
- Optional logging of multiset differences (slows down performance to O(n*m))

## Installation

Add the following to your `build.gradle.kts`:
```kotlin 
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.shme7ev:kotlin-multiset:master-SNAPSHOT")
}
```

[![](https://jitpack.io/v/shme7ev/kotlin-multiset.svg)](https://jitpack.io/#shme7ev/kotlin-multiset)


## Usage

```kotlin 
data class Dept(val id: String, val name: String)
// custom list of properties to compare departments
val deptProperties = listOf(Dept::id, Dept::name)

data class Address(val street: String, val city: String)

data class Person(
    val name: String,
    val age: Int,
    val address: Address? = null,
    val department: List<Dept>? = null,
)
// custom list of properties to compare persons
val personProperties = listOf(Person::name, Person::age, Person::department)

val dept1 = Dept("D1", "Engineering")
val dept2 = Dept("D2", "HR")
val dept3 = Dept("D3", "Research")

val depts12 = listOf(dept1, dept2)
val depts2 = listOf(dept2)
val depts3 = listOf(dept3)

val alice = Person("Alice", 25, department = depts12)
val bob = Person("Bob", 30, department = depts2)
val charlie = Person("Charlie", 35, department = depts3)

val multiSet1 =
    listOf(alice, charlie).toMultiSet(personProperties, mapOf(Person::department to deptProperties))
val multiSet2 = listOf(bob).toMultiSet(personProperties, mapOf(Person::department to deptProperties))

// Find difference 
val difference = multiSet1.difference(multiSet2)
assertEquals(listOf(alice, charlie), difference)
// Find intersection 
val intersection = multiSet1.intersect(multiSet2)

// log differences for each element comparison - performance drops to O(n*M)
val logs = mutableListOf<String>()
val difference2 = multiSet2.difference(multiSet1) { logs.add(it) }
logs.forEach { println(it) }

//For one-time regular list difference:
val oneTimeDiff = listOf(alice, charlie).difference(listOf(bob), personProperties, mapOf(Person::department to deptProperties))
```