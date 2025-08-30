package io.github.multiset

import org.junit.Assert.*
import org.junit.Test
import kotlin.reflect.KProperty1
import io.github.multiset.NestedPropertyMultiSet.Companion.toMultiSet

class MultiSetTest {

    data class Simple(val id: Int, val value: String)

    private val simpleId: KProperty1<Simple, Int> = Simple::id

    val idList = listOf(simpleId)


    @Test
    fun testSimpleIntersect() {
        val listA = listOf(Simple(1, "A"), Simple(2, "B"), Simple(3, "C"))
        val listB = listOf(Simple(2, "B"), Simple(4, "D"))

        val setA = listA.toMultiSet(listOf(simpleId))
        val setB = listB.toMultiSet(listOf(simpleId))

        val result = setA.intersect(setB)

        assertEquals(1, result.size)
        assertEquals(Simple(2, "B"), result[0])
    }

    @Test
    fun testSimpleDifference() {
        val listA = listOf(Simple(1, "A"), Simple(2, "B"), Simple(3, "C"))
        val listB = listOf(Simple(2, "B"))

        val setA = listA.toMultiSet(idList)
        val setB = listB.toMultiSet(idList)

        val result = setA.difference(setB)

        assertEquals(2, result.size)
        assertTrue(result.contains(Simple(1, "A")))
        assertTrue(result.contains(Simple(3, "C")))
    }
}
