package com.answufeng.arch.ext

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlowExtTest {

    @Test
    fun `select extracts field from StateFlow`() = runTest {
        val state = MutableStateFlow("hello" to 1)
        val selected = state.select { it.first }
        val result = selected.first()
        assertEquals("hello", result)
    }

    @Test
    fun `debounceAction delays emission`() = runTest {
        val source = flowOf(1)
        val result = source.debounceAction(0).first()
        assertEquals(1, result)
    }
}
