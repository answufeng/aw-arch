package com.answufeng.arch.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
        FlowEventBus.autoCleanupDelay = 30_000L
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

    @Test
    fun `observe without collect does not remove channel after cleanup delay`() = runBlocking {
        FlowEventBus.autoCleanupDelay = 80L
        FlowEventBus.observe<TestEvent>()
        delay(250)
        assertTrue(FlowEventBus.containsNormalChannel(TestEvent::class))
    }

    @Test
    fun `last collector cancelled removes normal channel after cleanup delay`() = runBlocking {
        FlowEventBus.autoCleanupDelay = 80L
        var job: Job? = null
        job = launch {
            FlowEventBus.observe<TestEvent>().collect { }
        }
        delay(20)
        job!!.cancel()
        job.join()
        delay(250)
        assertFalse(FlowEventBus.containsNormalChannel(TestEvent::class))
    }

    @Test
    fun `sticky channel cleanup does not remove normal channel for same class`() = runBlocking {
        FlowEventBus.autoCleanupDelay = 80L
        FlowEventBus.observe<TestEvent>()
        var stickyJob: Job? = null
        stickyJob = launch {
            FlowEventBus.observeSticky<TestEvent>().collect { }
        }
        delay(20)
        stickyJob!!.cancel()
        stickyJob.join()
        delay(250)
        assertFalse(FlowEventBus.containsStickyChannel(TestEvent::class))
        assertTrue(FlowEventBus.containsNormalChannel(TestEvent::class))
    }
}
