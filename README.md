# kotlin-multiset

A Kotlin library for working with multisets (bags) that allows comparing complex objects based on their properties. This library provides two main classes:

1. [PropertyBasedMultiSet] - A basic multiset implementation that compares elements based on specified properties
2. [NestedPropertyMultiSet] - An advanced multiset that can handle nested multisets in properties

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/shme7ev/kotlin-multiset)

## Features

- Compare complex objects based on selected properties
- Support for nested objects and nested multisets
- Operations like intersection, difference, symmetric difference
- Proper handling of BigDecimal comparisons
- Null-safe property comparisons

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

### Basic Property-Based Multiset

```kotlin 
data class Person(val name: String, val age: Int)
val person1 = Person("Alice", 25) 
val person2 = Person("Alice", 25) 
val person3 = Person("Bob", 30)
val properties = listOf(Person::name, Person::age) 
val multiSet1 = PropertyBasedMultiSet(listOf(person1, person2), properties) 
val multiSet2 = PropertyBasedMultiSet(listOf(person2, person3), properties)
// Find intersection 
val intersection = multiSet1.intersect(multiSet2)
// Find difference 
val difference = multiSet1.difference(multiSet2)
```
### Nested Property Multiset

```kotlin 
data class Address(val street: String, val city: String) 
data class Person(val name: String, val age: Int, val address: Address)
val address1 = Address("123 Main St", "Springfield") 
val address2 = Address("123 Main St", "Springfield") 
val person1 = Person("Alice", 25, address1) 
val person2 = Person("Alice", 25, address2)
val addressProperties = listOf(Address::street, Address::city) 
val personProperties = listOf(Person::name, Person::age, Person::address)
val multiSet1 = listOf(person1).toMultiSet(personProperties) 
val multiSet2 = listOf(person2).toMultiSet(personProperties)
// These will be equal because the addresses have the same properties 
assertTrue(multiSet1.elementsEquals(multiSet2))
```