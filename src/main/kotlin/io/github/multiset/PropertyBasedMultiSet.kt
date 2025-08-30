package io.github.multiset

import java.math.BigDecimal
import kotlin.reflect.KProperty1

open class PropertyBasedMultiSet<T : Any>(
    val elements: List<T>,
    protected val properties: List<KProperty1<T, *>>,
) {
    private val elementGroups: Map<Int, MutableList<T>> by lazy {
        elements.groupByTo(mutableMapOf(), ::hash).mapValues { it.value.toMutableList() }
    }

    protected fun elementGroups(): Map<Int, MutableList<T>> = elementGroups

    private val propertyNames = properties.map { it.name }

    protected open fun equality(a: T, b: T): Boolean =
        properties.all { prop ->
            val aValue = prop.get(a)
            val bValue = prop.get(b)
            when {
                aValue is BigDecimal && bValue is BigDecimal -> aValue.compareTo(bValue) == 0
                else -> aValue == bValue
            }
        }

    protected open fun hash(element: T): Int =
        properties.fold(0) { acc, prop ->
            val value = prop.get(element)
            31 * acc + when (value) {
                is BigDecimal -> value.stripTrailingZeros().hashCode()
                else -> value?.hashCode() ?: 0
            }
        }

    fun intersect(other: PropertyBasedMultiSet<T>): List<T> {
        require(this.propertyNames == other.propertyNames) {
            "Can only intersect MultiSets with the same property comparisons"
        }

        val result = mutableListOf<T>()
        val otherCopy = other.copyElementGroups()

        elementGroups.forEach { (hash, group) ->
            otherCopy[hash]?.let { otherGroup ->
                group.forEach { element ->
                    val matchingIndex = otherGroup.indexOfFirst { equality(element, it) }
                    if (matchingIndex >= 0) {
                        result.add(element)
                        otherGroup.removeAt(matchingIndex)
                    }
                }
            }
        }

        return result
    }

    fun difference(other: PropertyBasedMultiSet<T>): List<T> {
        require(this.propertyNames == other.propertyNames) {
            "Can only calculate difference between MultiSets with the same property comparisons"
        }

        val otherCopy = other.copyElementGroups()
        val resultElements = mutableListOf<T>()

        elementGroups.forEach { (hash, group) ->
            val remainingOtherGroup = otherCopy[hash] ?: mutableListOf()

            group.forEach { element ->
                val matchingIndex = remainingOtherGroup.indexOfFirst { equality(element, it) }
                if (matchingIndex == -1) {
                    resultElements.add(element)
                } else {
                    remainingOtherGroup.removeAt(matchingIndex)
                }
            }
        }

        return resultElements
    }


    // can be used for debugging
    fun differenceWithLogging(other: PropertyBasedMultiSet<T>, logger: (String) -> Unit): List<T> {
        require(this.propertyNames == other.propertyNames) {
            "Can only calculate difference between MultiSets with the same property comparisons"
        }

        val otherCopy = other.copyElementGroups()
        val resultElements = mutableListOf<T>()

        elementGroups.forEach { (hash, group) ->
            val remainingOtherGroup = otherCopy[hash] ?: mutableListOf()

            group.forEach { element ->
                val matchingIndex = remainingOtherGroup.indexOfFirst { equality(element, it) }
                if (matchingIndex == -1) {
                    // Log differences for each element in other
                    other.elements.forEach { otherElement ->
                        val differences = properties
                            // TODO needs to be adjusted for reference types and BigDecimals
                            .filter { prop -> prop.get(element) != prop.get(otherElement) }
                            .joinToString(", ") { prop ->
                                "${prop.name}: ${prop.get(element)} != ${prop.get(otherElement)}"
                            }
                        if (differences.isNotEmpty()) {
                            logger("No match for $element in other multiset, differences with $otherElement: $differences")
                        }
                    }
                    resultElements.add(element)
                } else {
                    remainingOtherGroup.removeAt(matchingIndex)
                }
            }
        }

        return resultElements
    }

    fun matchElements(other: PropertyBasedMultiSet<T>): Map<T, List<T>> {
        require(this.propertyNames == other.propertyNames) {
            "Can only match elements between MultiSets with the same property comparisons"
        }

        val result = mutableMapOf<T, List<T>>()
        val otherCopy = other.copyElementGroups()

        elementGroups.forEach { (hash, group) ->
            val otherGroup = otherCopy[hash] ?: mutableListOf()
            group.forEach { element ->
                val matches = otherGroup.filter { equality(element, it) }
                if (matches.isNotEmpty()) {
                    result[element] = matches
                }
            }
        }

        return result
    }

    fun symmetricDifference(other: PropertyBasedMultiSet<T>): Pair<Set<T>, Set<T>> {
        require(this.propertyNames == other.propertyNames) {
            "Can only calculate symmetric difference between MultiSets with the same property comparisons"
        }

        val firstOnly = mutableSetOf<T>()
        val secondOnly = mutableSetOf<T>()
        val thisCopy = copyElementGroups()
        val otherCopy = other.copyElementGroups()

        // Collect all unique hash codes from both multisets
        val allHashes = (thisCopy.keys + otherCopy.keys).toSet()

        allHashes.forEach { hash ->
            val thisGroup = thisCopy[hash] ?: mutableListOf()
            val otherGroup = otherCopy[hash] ?: mutableListOf()

            val thisGroupCopy = thisGroup.toMutableList()
            thisGroupCopy.forEach { thisElement ->
                val matchingIndex = otherGroup.indexOfFirst { equality(thisElement, it) }
                if (matchingIndex == -1) {
                    firstOnly.add(thisElement)
                } else {
                    otherGroup.removeAt(matchingIndex)
                }
            }

            // Remaining elements in otherGroup are unique to other
            secondOnly.addAll(otherGroup)
        }

        return Pair(firstOnly, secondOnly)
    }

    fun contains(element: T): Boolean {
        val group = elementGroups[hash(element)] ?: return false
        return group.any { equality(it, element) }
    }

    fun count(element: T): Int {
        val group = elementGroups[hash(element)] ?: return 0
        return group.count { equality(it, element) }
    }

    protected open fun copyElementGroups(): Map<Int, MutableList<T>> {
        return elementGroups.mapValues { it.value.toMutableList() }
    }

    override fun toString(): String = "PropertyBasedMultiSet(elements=$elements)"
}