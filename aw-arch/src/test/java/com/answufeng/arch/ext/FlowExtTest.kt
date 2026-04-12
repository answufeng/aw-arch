package com.answufeng.arch.ext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlowExtTest {

    @Test
    fun `throttleFirst emits first element in window`() = runTest {
        val flow = flowOf(1, 2, 3).throttleFirst(0)
        val results = mutableListOf<Int>()
        flow.collect { results.add(it) }
        assertEquals(listOf(1, 2, 3), results)
    }

    @Test
    fun `select extracts and deduplicates field`() = runTest {
        data class State(val count: Int, val name: String)

        val flow = flowOf(
            State(1, "a"),
            State(1, "b"),
            State(2, "b"),
            State(2, "c"),
        ).select { it.count }

        val results = mutableListOf<Int>()
        flow.collect { results.add(it) }
        assertEquals(listOf(1, 2), results)
    }

    @Test
    fun `debounceAction emits last element after timeout`() = runTest {
        val flow = flowOf("a", "b", "c").debounceAction(0)
        val results = mutableListOf<String>()
        flow.collect { results.add(it) }
        assertEquals(listOf("a", "b", "c"), results)
    }
}
