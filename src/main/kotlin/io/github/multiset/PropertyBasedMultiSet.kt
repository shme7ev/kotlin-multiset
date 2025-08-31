package io.github.multiset

import java.math.BigDecimal
import kotlin.reflect.KProperty1

open class PropertyBasedMultiSet<T : Any>(
    val elements: List<T>,
    protected val properties: List<KProperty1<T, *>>,
) {

    private val elementGroups by lazy { elements.groupBy { hash(it) } }

    private val propertyNames by lazy { properties.map { it.name } }

    protected open fun equality(a: T, b: T): Boolean =
        properties.all { prop -> propertyCompare(prop.get(a), prop.get(b)) }

    private fun propertyCompare(aValue: Any?, bValue: Any?) = when {
        aValue is BigDecimal && bValue is BigDecimal -> aValue.compareTo(bValue) == 0
        else -> aValue == bValue
    }

    private fun ((String) -> Unit).logDifferences(element: T, otherElement: T) {
        properties
            .filter { prop -> !propertyCompare(prop.get(element), prop.get(otherElement)) }
            .joinToString(", ") { prop ->
                "${prop.name}: ${prop.get(element)} != ${prop.get(otherElement)}"
            }.takeIf { it.isNotEmpty() }?.let {
                this("No match for $element in other multiset, differences with $otherElement: $it")
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

        elementGroups.forEach { (hash, group) ->
            other.elementGroups[hash]?.let { otherGroup ->
                when {
                    group.size == 1 && otherGroup.size == 1 -> result.add(group.first())
                    else -> {
                        // Hash collision - need equality check
                        group.forEach { element ->
                            val copyOtherGroup = otherGroup.toMutableList()
                            val matchingIndex = copyOtherGroup.indexOfFirst { equality(element, it) }
                            if (matchingIndex >= 0) {
                                result.add(element)
                                copyOtherGroup.removeAt(matchingIndex)
                            }
                        }
                    }
                }
            }
        }

        return result
    }

    fun difference(other: PropertyBasedMultiSet<T>, logger: ((String) -> Unit)? = null): List<T> {
        elements
        require(this.propertyNames == other.propertyNames) {
            "Can only calculate difference between MultiSets with the same property comparisons"
        }

        val resultElements = mutableListOf<T>()

        elementGroups.forEach { (hash, hashGroup) ->
            val otherHashGroup = other.elementGroups[hash]
            if (otherHashGroup == null) {
                resultElements.addAll(hashGroup)
                logger?.run {
                    hashGroup.forEach { element ->
                        other.elements.forEach { otherElement ->
                            logDifferences(element, otherElement)
                        }
                    }
                }
            } else {
                if (hashGroup.size == 1 && otherHashGroup.size == 1) {
                    // hash match, skip since no difference
                } else {
                    // hash collision
                    val copyOtherHashGroup = otherHashGroup.toMutableList()
                    hashGroup.forEach { element ->
                        val matchingIndex = copyOtherHashGroup.indexOfFirst { equality(element, it) }
                        if (matchingIndex == -1) {
                            resultElements.add(element)
                            logger?.run {
                                copyOtherHashGroup.forEachIndexed { index, otherElement ->
                                    if (index != matchingIndex) logDifferences(element, otherElement)
                                }
                            }
                        } else {
                            copyOtherHashGroup.removeAt(matchingIndex)
                        }
                    }
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
            otherCopy[hash]?.let { otherGroup ->
                if (group.size == 1 && otherGroup.size == 1) result[group.first()] = otherGroup
                else {
                    // hash collision, have to compare by equality
                    group.forEach { element ->
                        result[element] = otherGroup.filter { equality(element, it) }
                    }
                }
            }
        }

        return result
    }

    fun symmetricDifference(other: PropertyBasedMultiSet<T>): Pair<List<T>, List<T>> {
        require(this.propertyNames == other.propertyNames) {
            "Can only calculate symmetric difference between MultiSets with the same property comparisons"
        }

        val firstOnly = mutableListOf<T>()
        val secondOnly = mutableListOf<T>()

        // Collect all unique hash codes from both multisets
        val allHashes = (elementGroups.keys + other.elementGroups.keys).toSet()

        allHashes.forEach { hash ->
            val thisGroup = elementGroups[hash] ?: listOf()
            val otherGroup = other.elementGroups[hash] ?: listOf()

            when {
                thisGroup.isEmpty() -> secondOnly.addAll(otherGroup)
                otherGroup.isEmpty() -> firstOnly.addAll(thisGroup)
                thisGroup.size == 1 && otherGroup.size == 1 -> {
                    // match, do nothing
                }

                else -> {
                    val thisGroupCopy = thisGroup.toMutableList()
                    val otherGroupCopy = otherGroup.toMutableList()
                    thisGroupCopy.forEach { thisElement ->
                        val matchingIndex = otherGroupCopy.indexOfFirst { equality(thisElement, it) }
                        if (matchingIndex == -1) {
                            firstOnly.add(thisElement)
                        } else {
                            otherGroupCopy.removeAt(matchingIndex)
                        }
                    }

                    // Remaining elements in otherGroup are unique to other
                    secondOnly.addAll(otherGroupCopy)
                }

            }
        }

        return Pair(firstOnly, secondOnly)
    }

    fun contains(element: T): Boolean = elementGroups[hash(element)] != null

    fun count(element: T): Int {
        val group = elementGroups[hash(element)]

        return when {
            group == null -> 0
            group.size == 1 -> 1
            else -> group.count { equality(it, element) }
        }
    }

    protected open fun copyElementGroups(): Map<Int, MutableList<T>> {
        return elementGroups.mapValues { it.value.toMutableList() }
    }

    override fun toString(): String = "PropertyBasedMultiSet(elements=$elements)"
}