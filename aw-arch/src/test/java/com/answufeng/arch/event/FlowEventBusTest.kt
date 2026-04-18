package com.answufeng.arch.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.After
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlowEventBusTest {

    data class TestEvent(val value: String)
    data class StickyEvent(val data: Int)

    @After
    fun tearDown() {
        FlowEventBus.clearAll()
    }

    @Test
    fun `tryPost returns true`() {
        val result = FlowEventBus.tryPost(TestEvent("hello"))
        assertTrue(result)
    }

    @Test
    fun `observe returns Flow`() = runTest {
        val flow = FlowEventBus.observe<TestEvent>()
        assertNotNull(flow)
    }

    @Test
    fun `observeSticky returns Flow`() = runTest {
        val flow = FlowEventBus.observeSticky<StickyEvent>()
        assertNotNull(flow)
    }

    @Test
    fun `postSticky and observeSticky works`() = runTest {
        FlowEventBus.postSticky(StickyEvent(42))
        advanceUntilIdle()

        val event = FlowEventBus.observeSticky<StickyEvent>().first()
        assertEquals(42, event.data)
    }

    @Test
    fun `removeSticky removes sticky event`() = runTest {
        FlowEventBus.postSticky(StickyEvent(99))
        advanceUntilIdle()

        FlowEventBus.removeSticky<StickyEvent>()
    }

    @Test
    fun `clearAll removes all events`() {
        FlowEventBus.clearAll()
    }

    @Test
    fun `clear removes specific event type`() {
        FlowEventBus.clear(TestEvent::class)
    }
}
