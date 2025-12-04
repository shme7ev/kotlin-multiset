package io.github.multiset

import java.math.BigDecimal
import kotlin.reflect.KProperty1

open class PropertyBasedMultiSet<T : Any>(
    val elements: List<T>,
    private val properties: List<KProperty1<T, *>>,
    private val multisetProperties: Map<KProperty1<T, *>, List<KProperty1<*, *>>> = emptyMap(),
) {

    private val elementGroups by lazy { elements.groupBy { hash(it) } }

    private val propertyNames by lazy { properties.map { it.name } }

    protected open fun equality(a: T, b: T): Boolean =
        properties.all { prop ->
            val aValue = prop.get(a)
            val bValue = prop.get(b)
            when {
                aValue == null && bValue == null -> true
                aValue == null || bValue == null -> false
                multisetProperties.containsKey(prop) -> {
                    val elementProperties = multisetProperties[prop]
                    if (elementProperties != null) {
                        val aList = aValue as? List<*>
                        val bList = bValue as? List<*>
                        when {
                            aList == null && bList == null -> true
                            aList == null || bList == null -> false
                            else -> {
                                @Suppress("UNCHECKED_CAST")
                                val aMultiSet = PropertyBasedMultiSet(
                                    aList.filterNotNull(),
                                    elementProperties as List<KProperty1<Any, *>>
                                )
                                val bMultiSet = PropertyBasedMultiSet(bList.filterNotNull(), elementProperties)
                                aMultiSet.elementsEquals(bMultiSet)
                            }
                        }
                    } else aValue == bValue
                }

                aValue is BigDecimal && bValue is BigDecimal -> aValue.compareTo(bValue) == 0
                else -> aValue == bValue
            }
        }

    private fun propertyCompare(aValue: Any?, bValue: Any?) = when {
        aValue is BigDecimal && bValue is BigDecimal -> aValue.compareTo(bValue) == 0
        else -> aValue == bValue
    }

    private fun ((String) -> Unit).logDifferences(element: T, otherElement: T) {
        val mainDifferences = properties
            .filter { prop -> !propertyCompare(prop.get(element), prop.get(otherElement)) }
            .joinToString(separator = "\n", prefix = "\n") { prop ->
                "${prop.name}: ${prop.get(element)} != ${prop.get(otherElement)}"
            }

        val nestedDifferences = properties
            .filter { prop -> multisetProperties.containsKey(prop) }
            .joinToString(separator = "\n", prefix = "\n") { prop ->
                val aValue = prop.get(element)
                val bValue = prop.get(otherElement)
                if (aValue != bValue) {
                    val elementProperties = multisetProperties[prop]
                    if (elementProperties != null) {
                        val aList = aValue as? List<*>
                        val bList = bValue as? List<*>
                        when {
                            aList == null && bList == null -> {
                                // Both null, no difference to log
                                ""
                            }

                            aList == null || bList == null -> {
                                "No match for $element in other multiset, differences with $otherElement: ${prop.name}: one is null, other is $aValue vs $bValue"
                            }

                            aList.size != bList.size -> {
                                "List sizes are different ${aList.size} ${bList.size}"
                            }

                            else -> {

                                var diffs = ""

                                aList.forEach { nestedElement1 ->
                                    bList.forEach { nestedElement2 ->
                                        @Suppress("UNCHECKED_CAST")
                                        diffs += (elementProperties as List<KProperty1<Any, *>>).filter { nestedProp ->
                                            !propertyCompare(
                                                nestedProp.get(nestedElement1 as Any),
                                                nestedProp.get(nestedElement2 as Any)
                                            )
                                        }.joinToString(prefix = "\n", separator = "\n") { nestedProp ->
                                            "${nestedProp.name}: ${nestedProp.get(nestedElement1 as Any)} != ${
                                                nestedProp.get(
                                                    nestedElement2 as Any
                                                )
                                            }"
                                        }
                                    }
                                }
                                diffs
                            }
                        }
                    } else ""
                } else ""
            }

        (mainDifferences + nestedDifferences).takeIf { it.isNotEmpty() }?.let {
            this("No match for $element in other multiset, differences with $otherElement: $it")
        }
    }

    protected open fun hash(element: T): Int =
        properties.fold(0) { acc, prop ->
            val value = prop.get(element)
            31 * acc + when (value) {
                is BigDecimal -> value.stripTrailingZeros().hashCode()
                else -> {
                    if (multisetProperties.containsKey(prop)) {
                        val elementProperties = multisetProperties[prop]
                        if (elementProperties != null) {
                            val list = value as? List<*>
                            if (list != null) {
                                @Suppress("UNCHECKED_CAST")
                                val multiSet = PropertyBasedMultiSet(
                                    list.filterNotNull(),
                                    elementProperties as List<KProperty1<Any, *>>
                                )
                                multiSet.contentHashCode()
                            } else {
                                value?.hashCode() ?: 0
                            }
                        } else {
                            value?.hashCode() ?: 0
                        }
                    } else {
                        value?.hashCode() ?: 0
                    }
                }
            }
        }

    fun contentHashCode(): Int = elements.map(::hash).sorted().fold(1) { acc, h -> 31 * acc + h }

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

    /**
     * Computes the difference between this multiset and another multiset
     *
     * This function finds elements that exist in this multiset but not in the other multiset.
     * It uses hash values and equality comparisons to determine if elements exist in both sets.
     *
     * @param other The other multiset to compute difference with
     * @param logger Optional logger function to record information about element differences
     * CAUTION: PASSING LOGGER WILL TURN THIS INTO O(N*M) !!!
     * @return A list of elements that exist in this multiset but not in the other
     * @throws IllegalArgumentException If the two multisets don't have the same property comparators
     */
    fun difference(other: PropertyBasedMultiSet<T>, logger: ((String) -> Unit)? = null): List<T> {
        elements
        require(this.propertyNames == other.propertyNames) {
            "Can only calculate difference between MultiSets with the same property comparisons"
        }

        val resultElements = mutableListOf<T>()

        elementGroups.forEach { (hash, hashGroup) ->
            val otherHashGroup = other.elementGroups[hash]
            if (otherHashGroup == null) {
                // Current hash group doesn't exist in the other set, add all elements to result
                resultElements.addAll(hashGroup)
                logger?.run {
                    hashGroup.forEach { element ->
                        other.elements.forEach { otherElement -> logDifferences(element, otherElement) }
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
//                Since the hash is the same and there's only one element in each group, they're considered to match without further verification
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

    fun elementsEquals(other: PropertyBasedMultiSet<T>, logger: ((String) -> Unit)? = null): Boolean {
        if (this.elements.size != other.elements.size) {
            logger?.let { it ->
                it("No match for $this vs $other - sizes are different: ${this.elements.size} ${other.elements.size}")
            }
            return false
        }

        if (elementGroups.size != other.elementGroups.size) {
            logger?.let { it ->
                this.elements.forEach { element ->
                    other.elements.forEach { otherElement -> it.logDifferences(element, otherElement) }
                }
            }
            return false
        }

        for ((hashCode, thisGroup) in elementGroups) {
            val otherGroup = other.elementGroups[hashCode] ?: run {
                logger?.run {
                    thisGroup.forEach { element ->
                        other.elements.forEach { otherElement -> logDifferences(element, otherElement) }
                    }
                }
                return false
            }

            if (thisGroup.size != otherGroup.size) {
                logger?.run {
                    thisGroup.forEach { element ->
                        other.elements.forEach { otherElement -> logDifferences(element, otherElement) }
                    }
                }
                return false
            }

            // If both groups have size 1, no need for further equality checks
            if (thisGroup.size == 1) continue

            // For larger groups, we need to check element equality
            val otherGroupCopy = otherGroup.toMutableList()

            for (element in thisGroup) {
                val matchingIndex = otherGroupCopy.indexOfFirst { otherElement ->
                    equality(element, otherElement)
                }

                if (matchingIndex == -1) {
                    logger?.run {
                        otherGroupCopy.forEachIndexed { index, otherElement ->
                            if (index != matchingIndex) logDifferences(element, otherElement)
                        }
                    }
                    return false
                }
                otherGroupCopy.removeAt(matchingIndex)
            }
        }

        return true
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

    companion object {
        fun <T : Any> List<T>.toMultiSet(
            properties: List<KProperty1<T, *>>,
            multisetProperties: Map<KProperty1<T, *>, List<KProperty1<*, *>>> = emptyMap(),
        ): PropertyBasedMultiSet<T> {
            require(properties.isNotEmpty()) { "At least one property must be specified" }
            return PropertyBasedMultiSet(this, properties.toList(), multisetProperties)
        }

        fun <T : Any> List<T>.difference(
            other: List<T>,
            properties: List<KProperty1<T, *>>,
            multisetProperties: Map<KProperty1<T, *>, List<KProperty1<*, *>>> = emptyMap(),
            logger: ((String) -> Unit)? = null,
        ) = toMultiSet(properties, multisetProperties).difference(
            other.toMultiSet(properties, multisetProperties),
            logger
        )

        fun <T : Any> List<T>.intersect(
            other: List<T>,
            properties: List<KProperty1<T, *>>,
            multisetProperties: Map<KProperty1<T, *>, List<KProperty1<*, *>>> = emptyMap(),
        ) = toMultiSet(properties, multisetProperties).intersect(
            other.toMultiSet(properties, multisetProperties),
        )

        fun <T : Any> List<T>.matchElements(
            other: List<T>,
            properties: List<KProperty1<T, *>>,
            multisetProperties: Map<KProperty1<T, *>, List<KProperty1<*, *>>> = emptyMap(),
        ) = toMultiSet(properties, multisetProperties).matchElements(
            other.toMultiSet(properties, multisetProperties)
        )

        fun <T : Any> List<T>.symmetricDifference(
            other: List<T>,
            properties: List<KProperty1<T, *>>,
            multisetProperties: Map<KProperty1<T, *>, List<KProperty1<*, *>>> = emptyMap(),
            logger: ((String) -> Unit)? = null,
        ) = toMultiSet(properties, multisetProperties).symmetricDifference(
            other.toMultiSet(properties, multisetProperties),
        )

        fun <T : Any> List<T>.matchLists(
            other: List<T>, properties: List<KProperty1<T, *>>,
            multisetProperties: Map<KProperty1<T, *>, List<KProperty1<*, *>>> = emptyMap(),
            logger: ((String) -> Unit)? = null,
        ) = toMultiSet(properties, multisetProperties).elementsEquals(
            other.toMultiSet(properties, multisetProperties),
            logger
        )

    }
}