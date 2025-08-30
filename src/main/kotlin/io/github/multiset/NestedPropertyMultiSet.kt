package io.github.multiset

import java.math.BigDecimal
import kotlin.reflect.KProperty1

class NestedPropertyMultiSet<T : Any>(
    elements: List<T>,
    properties: List<KProperty1<T, *>>,
) : PropertyBasedMultiSet<T>(elements, properties) {

    companion object {
        fun <T : Any> List<T>.toMultiSet(
            properties: List<KProperty1<T, *>>,
        ): NestedPropertyMultiSet<T> {
            require(properties.isNotEmpty()) { "At least one property must be specified" }
            return NestedPropertyMultiSet(this, properties.toList())
        }
    }

    override fun equality(a: T, b: T): Boolean =
        properties.all { prop ->
            val aValue = prop.get(a)
            val bValue = prop.get(b)
            when {
                aValue == null && bValue == null -> true
                aValue == null || bValue == null -> false
                aValue is NestedPropertyMultiSet<*> && bValue is NestedPropertyMultiSet<*> -> aValue.elementsEquals(
                    bValue
                )
                aValue is BigDecimal && bValue is BigDecimal -> aValue.compareTo(bValue) == 0
                else -> aValue == bValue
            }
        }

    override fun hash(element: T): Int =
        properties.fold(0) { acc, prop ->
            val value = prop.get(element)
            31 * acc + when (value) {
                is NestedPropertyMultiSet<*> -> value.contentHashCode()
                is BigDecimal -> value.stripTrailingZeros().hashCode()
                else -> value?.hashCode() ?: 0
            }
        }

    fun elementsEquals(other: NestedPropertyMultiSet<*>): Boolean {
        if (this.elements.size != other.elements.size) return false

        val otherGroups = other.elementGroups()
        val thisGroups = elementGroups()

        if (thisGroups.size != otherGroups.size) return false

        for ((hashCode, thisGroup) in thisGroups) {
            val otherGroup = otherGroups[hashCode] ?: return false

            if (thisGroup.size != otherGroup.size) return false

            val otherGroupCopy = otherGroup.toMutableList()

            for (element in thisGroup) {
                val matchingIndex = otherGroupCopy.indexOfFirst { otherElement ->
                    equality(element, otherElement as T)
                }

                if (matchingIndex == -1) return false
                otherGroupCopy.removeAt(matchingIndex)
            }
        }

        return true
    }


    fun contentHashCode(): Int = elements.fold(0) { acc, element -> 31 * acc + hash(element) }
}
