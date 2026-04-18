package com.answufeng.arch.mvi

import com.answufeng.arch.test.AwTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {

    @get:Rule
    val awTestRule = AwTestRule()

    data class TestState(
        val count: Int = 0,
        val loading: Boolean = false
    ) : UiState

    sealed class TestEvent : UiEvent {
        data class ShowToast(val msg: String) : TestEvent()
    }

    sealed class TestIntent : UiIntent {
        data object Increment : TestIntent()
        data object Decrement : TestIntent()
        data object TriggerEvent : TestIntent()
        data object ThrowError : TestIntent()
        data object LoadData : TestIntent()
    }

    class TestViewModel : MviViewModel<TestState, TestEvent, TestIntent>(TestState()) {
        var lastError: Throwable? = null
        private var fakeTime = 0L

        override fun currentTimeMillis(): Long = fakeTime

        fun advanceTime(millis: Long) {
            fakeTime += millis
        }

        override fun handleIntent(intent: TestIntent) {
            when (intent) {
                TestIntent.Increment -> updateState { copy(count = count + 1) }
                TestIntent.Decrement -> updateState { copy(count = count - 1) }
                TestIntent.TriggerEvent -> sendMviEvent(TestEvent.ShowToast("hello"))
                TestIntent.ThrowError -> launch { throw RuntimeException("test error") }
                TestIntent.LoadData -> {
                    updateState { copy(loading = true) }
                    launchIO {
                        updateState { copy(loading = false, count = 100) }
                    }
                }
            }
        }

        override fun handleException(throwable: Throwable) {
            lastError = throwable
        }

        fun testClearThrottleCache() = clearThrottleCache()
    }

    @Test
    fun `initial state is correct`() {
        val vm = TestViewModel()
        assertEquals(TestState(count = 0, loading = false), vm.state.value)
    }

    @Test
    fun `dispatch Increment updates state`() {
        val vm = TestViewModel()
        vm.dispatch(TestIntent.Increment)
        assertEquals(1, vm.state.value.count)
    }

    @Test
    fun `dispatch Decrement updates state`() {
        val vm = TestViewModel()
        vm.dispatch(TestIntent.Decrement)
        assertEquals(-1, vm.state.value.count)
    }

    @Test
    fun `multiple dispatches accumulate state changes`() {
        val vm = TestViewModel()
        vm.dispatch(TestIntent.Increment)
        vm.dispatch(TestIntent.Increment)
        vm.dispatch(TestIntent.Increment)
        vm.dispatch(TestIntent.Decrement)
        assertEquals(2, vm.state.value.count)
    }

    @Test
    fun `sendEvent emits event via channel`() = runTest {
        val vm = TestViewModel()
        vm.dispatch(TestIntent.TriggerEvent)
        advanceUntilIdle()

        val event = vm.event.first()
        assertTrue(event is TestEvent.ShowToast)
        assertEquals("hello", (event as TestEvent.ShowToast).msg)
    }

    @Test
    fun `handleException is called on coroutine error`() = runTest {
        val vm = TestViewModel()
        vm.dispatch(TestIntent.ThrowError)
        advanceUntilIdle()

        assertNotNull(vm.lastError)
        assertEquals("test error", vm.lastError?.message)
    }

    @Test
    fun `launchIO updates state after completion`() = runTest {
        val vm = TestViewModel()
        vm.dispatch(TestIntent.LoadData)
        advanceUntilIdle()

        val deadline = System.currentTimeMillis() + 5_000L
        while (vm.state.value.loading && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }

        assertFalse(vm.state.value.loading)
        assertEquals(100, vm.state.value.count)
    }

    @Test
    fun `dispatchThrottled ignores rapid duplicate intents`() {
        val vm = TestViewModel()
        vm.advanceTime(100)
        vm.dispatchThrottled(TestIntent.Increment, 300)
        assertEquals(1, vm.state.value.count)

        vm.dispatchThrottled(TestIntent.Increment, 300)
        assertEquals(1, vm.state.value.count)

        vm.advanceTime(301)
        vm.dispatchThrottled(TestIntent.Increment, 300)
        assertEquals(2, vm.state.value.count)
    }

    @Test
    fun `dispatchThrottled allows different intent types`() {
        val vm = TestViewModel()
        vm.dispatchThrottled(TestIntent.Increment, 300)
        vm.dispatchThrottled(TestIntent.Decrement, 300)
        assertEquals(0, vm.state.value.count)
    }

    @Test
    fun `clearThrottleCache allows previously throttled intents`() {
        val vm = TestViewModel()
        vm.advanceTime(100)
        vm.dispatchThrottled(TestIntent.Increment, 300)
        assertEquals(1, vm.state.value.count)

        vm.dispatchThrottled(TestIntent.Increment, 300)
        assertEquals(1, vm.state.value.count)

        vm.testClearThrottleCache()

        vm.dispatchThrottled(TestIntent.Increment, 300)
        assertEquals(2, vm.state.value.count)
    }

    @Test
    fun `updateState is atomic`() {
        val vm = TestViewModel()
        repeat(100) {
            vm.dispatch(TestIntent.Increment)
        }
        assertEquals(100, vm.state.value.count)
    }
}
