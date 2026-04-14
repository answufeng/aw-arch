package com.answufeng.arch.mvi

import com.answufeng.arch.test.AwTestRule
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class MviInterfacesTest {

    @Test
    fun `UiState marker interface can be implemented`() {
        val state = TestMviState(42)
        assertEquals(42, state.count)
    }

    @Test
    fun `UiEvent marker interface can be implemented`() {
        val event = TestMviEvent.ShowMessage("hello")
        assertEquals("hello", event.msg)
    }

    @Test
    fun `UiIntent marker interface can be implemented`() {
        val intent = TestMviIntent.Load("123")
        assertEquals("123", intent.id)
    }

    @Test
    fun `SimpleMviViewModel updates state`() {
        val vm = TestSimpleMviVM()
        vm.dispatch(TestMviIntent.Increment)
        assertEquals(1, vm.state.value.count)
    }

    @Test
    fun `SimpleMviViewModel dispatchThrottled works`() {
        val vm = TestSimpleMviVM()
        vm.dispatchThrottled(TestMviIntent.Increment, 300)
        assertEquals(1, vm.state.value.count)
        vm.dispatchThrottled(TestMviIntent.Increment, 300)
        assertEquals(1, vm.state.value.count)
    }
}

data class TestMviState(val count: Int = 0) : UiState

sealed class TestMviEvent : UiEvent {
    data class ShowMessage(val msg: String) : TestMviEvent()
}

sealed class TestMviIntent : UiIntent {
    data object Increment : TestMviIntent()
    data object Refresh : TestMviIntent()
    data class Load(val id: String) : TestMviIntent()
}

class TestSimpleMviVM : SimpleMviViewModel<TestMviState, TestMviIntent>(TestMviState()) {
    override fun handleIntent(intent: TestMviIntent) {
        when (intent) {
            TestMviIntent.Increment -> updateState { copy(count = count + 1) }
            TestMviIntent.Refresh -> updateState { copy(count = 0) }
            is TestMviIntent.Load -> updateState { copy(count = count + (intent.id.toIntOrNull() ?: 0)) }
        }
    }
}
