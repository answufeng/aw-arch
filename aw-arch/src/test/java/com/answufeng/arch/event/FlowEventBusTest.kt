package com.answufeng.arch.event

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlowEventBusTest {

    data class TestEvent(val value: String)
    data class OtherEvent(val id: Int)

    @Before
    fun setup() {
        FlowEventBus.clear()
    }

    @After
    fun tearDown() {
        FlowEventBus.clear()
    }

    // ==================== 普通事件 ====================

    @Test
    fun `post and observe normal event`() = runTest {
        var received: TestEvent? = null

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observe<TestEvent>().first().also { received = it }
        }

        FlowEventBus.post(TestEvent("hello"))
        job.join()

        assertNotNull(received)
        assertEquals("hello", received?.value)
    }

    @Test
    fun `events of different types are isolated`() = runTest {
        var testReceived = false
        var otherReceived = false

        val job1 = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observe<TestEvent>().first()
            testReceived = true
        }

        val job2 = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observe<OtherEvent>().first()
            otherReceived = true
        }

        FlowEventBus.post(TestEvent("hello"))
        job1.join()

        assertTrue(testReceived)
        assertFalse(otherReceived)

        job2.cancel()
    }

    // ==================== tryPost ====================

    @Test
    fun `tryPost sends event successfully`() = runTest {
        var received: TestEvent? = null

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observe<TestEvent>().first().also { received = it }
        }

        val result = FlowEventBus.tryPost(TestEvent("tryPost"))
        job.join()

        assertTrue(result)
        assertNotNull(received)
        assertEquals("tryPost", received?.value)
    }

    // ==================== 粘性事件 ====================

    @Test
    fun `sticky event replays to new subscriber`() = runTest {
        FlowEventBus.postSticky(TestEvent("sticky"))

        var received: TestEvent? = null
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observeSticky<TestEvent>().first().also {
                received = it
            }
        }
        job.join()

        assertNotNull(received)
        assertEquals("sticky", received?.value)
    }

    @Test
    fun `tryPostSticky sends sticky event`() = runTest {
        val result = FlowEventBus.tryPostSticky(TestEvent("trySticky"))
        assertTrue(result)

        var received: TestEvent? = null
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observeSticky<TestEvent>().first().also {
                received = it
            }
        }
        job.join()

        assertNotNull(received)
        assertEquals("trySticky", received?.value)
    }

    @Test
    fun `removeSticky clears sticky event channel`() {
        FlowEventBus.removeSticky(TestEvent::class.java.name)
    }

    // ==================== 类型安全 observe ====================

    @Test
    fun `observe returns type-safe flow without manual cast`() = runTest {
        var received: TestEvent? = null

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observe<TestEvent>().first().also { received = it }
        }

        FlowEventBus.post(TestEvent("type-safe"))
        job.join()

        assertNotNull(received)
        assertEquals("type-safe", received?.value)
    }

    @Test
    fun `observe filters out wrong types`() = runTest {
        var testReceived: TestEvent? = null

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observe<TestEvent>().first().also { testReceived = it }
        }

        FlowEventBus.post(OtherEvent(1))
        FlowEventBus.post(TestEvent("correct"))
        job.join()

        assertNotNull(testReceived)
        assertEquals("correct", testReceived?.value)
    }

    // ==================== clear ====================

    @Test
    fun `clear removes all channels`() = runTest {
        FlowEventBus.post(TestEvent("one"))
        FlowEventBus.postSticky(OtherEvent(1))

        FlowEventBus.clear()

        var received = false
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observe<TestEvent>().first()
            received = true
        }

        FlowEventBus.post(TestEvent("two"))
        job.join()

        assertTrue(received)
    }

    // ==================== 自定义 key ====================

    @Test
    fun `custom key isolates same event type`() = runTest {
        var receivedA: TestEvent? = null
        var receivedB: TestEvent? = null

        val jobA = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observe<TestEvent>("channel_A").first().also { receivedA = it }
        }

        val jobB = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observe<TestEvent>("channel_B").first().also { receivedB = it }
        }

        FlowEventBus.post(TestEvent("forA"), key = "channel_A")
        jobA.join()

        assertNotNull(receivedA)
        assertEquals("forA", receivedA?.value)
        assertNull(receivedB)

        jobB.cancel()
    }

    // ==================== observeRaw ====================

    @Test
    fun `observeRaw returns SharedFlow with Any type`() = runTest {
        var received: Any? = null

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            FlowEventBus.observeRaw(TestEvent::class.java.name).first().also { received = it }
        }

        FlowEventBus.post(TestEvent("raw"))
        job.join()

        assertNotNull(received)
        assertTrue(received is TestEvent)
    }
}
