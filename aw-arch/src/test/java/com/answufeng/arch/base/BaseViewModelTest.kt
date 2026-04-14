package com.answufeng.arch.base

import com.answufeng.arch.test.AwTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @get:Rule
    val awTestRule = AwTestRule()

    class TestViewModel(savedStateHandle: androidx.lifecycle.SavedStateHandle? = null) :
        BaseViewModel(savedStateHandle) {

        var exceptionHandled: Throwable? = null

        fun testLaunch(block: suspend () -> Unit) = launch { block() }

        fun testLaunchIO(block: suspend () -> Unit) = launchIO { block() }

        fun testLaunchDefault(block: suspend () -> Unit) = launchDefault { block() }

        fun testLaunchWithError(block: suspend () -> Unit) =
            launch(onError = { exceptionHandled = it }) { block() }

        fun testShowToast(msg: String) = showToast(msg)

        fun testShowLoading(show: Boolean) = showLoading(show)

        fun testNavigate(route: String) = navigate(route)

        fun testNavigateBack() = navigateBack()

        fun testSendEvent(event: UIEvent) = sendEvent(event)

        fun testGetSavedState(key: String): String? = getSavedState(key)

        fun testSetSavedState(key: String, value: String) = setSavedState(key, value)

        override fun handleException(throwable: Throwable) {
            exceptionHandled = throwable
        }
    }

    // ==================== launch tests ====================

    @Test
    fun `launch executes block`() = runTest {
        val vm = TestViewModel()
        var executed = false
        vm.testLaunch { executed = true }
        advanceUntilIdle()
        assertTrue(executed)
    }

    @Test
    fun `launch handles exception via handleException`() = runTest {
        val vm = TestViewModel()
        vm.testLaunch { throw RuntimeException("test") }
        advanceUntilIdle()
        assertNotNull(vm.exceptionHandled)
        assertEquals("test", vm.exceptionHandled?.message)
    }

    @Test
    fun `launch with onError callback`() = runTest {
        val vm = TestViewModel()
        vm.testLaunchWithError { throw RuntimeException("custom") }
        advanceUntilIdle()
        assertNotNull(vm.exceptionHandled)
        assertEquals("custom", vm.exceptionHandled?.message)
    }

    // ==================== launchIO tests ====================

    @Test
    fun `launchIO handles exception`() = runTest {
        val vm = TestViewModel()
        vm.testLaunchIO { throw RuntimeException("io error") }
        advanceUntilIdle()
        assertNotNull(vm.exceptionHandled)
        assertEquals("io error", vm.exceptionHandled?.message)
    }

    // ==================== launchDefault tests ====================

    @Test
    fun `launchDefault handles exception`() = runTest {
        val vm = TestViewModel()
        vm.testLaunchDefault { throw RuntimeException("default error") }
        val deadline = System.currentTimeMillis() + 3000L
        while (vm.exceptionHandled == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        assertNotNull(vm.exceptionHandled)
        assertEquals("default error", vm.exceptionHandled?.message)
    }

    // ==================== UIEvent tests ====================

    @Test
    fun `showToast sends Toast event`() = runTest {
        val vm = TestViewModel()
        vm.testShowToast("hello")
        advanceUntilIdle()
        val event = vm.uiEvent.first()
        assertTrue(event is BaseViewModel.UIEvent.Toast)
        assertEquals("hello", (event as BaseViewModel.UIEvent.Toast).message)
    }

    @Test
    fun `showLoading sends Loading event`() = runTest {
        val vm = TestViewModel()
        vm.testShowLoading(true)
        advanceUntilIdle()
        val event = vm.uiEvent.first()
        assertTrue(event is BaseViewModel.UIEvent.Loading)
        assertTrue((event as BaseViewModel.UIEvent.Loading).show)
    }

    @Test
    fun `navigate sends Navigate event`() = runTest {
        val vm = TestViewModel()
        vm.testNavigate("/detail")
        advanceUntilIdle()
        val event = vm.uiEvent.first()
        assertTrue(event is BaseViewModel.UIEvent.Navigate)
        assertEquals("/detail", (event as BaseViewModel.UIEvent.Navigate).route)
    }

    @Test
    fun `navigateBack sends NavigateBack event`() = runTest {
        val vm = TestViewModel()
        vm.testNavigateBack()
        advanceUntilIdle()
        val event = vm.uiEvent.first()
        assertTrue(event is BaseViewModel.UIEvent.NavigateBack)
    }

    @Test
    fun `sendEvent sends Custom event`() = runTest {
        val vm = TestViewModel()
        vm.testSendEvent(BaseViewModel.UIEvent.Custom("key", "data"))
        advanceUntilIdle()
        val event = vm.uiEvent.first()
        assertTrue(event is BaseViewModel.UIEvent.Custom)
        assertEquals("key", (event as BaseViewModel.UIEvent.Custom).key)
        assertEquals("data", event.data)
    }

    // ==================== SavedStateHandle tests ====================

    @Test
    fun `savedStateHandle get and set`() {
        val handle = androidx.lifecycle.SavedStateHandle()
        val vm = TestViewModel(handle)
        vm.testSetSavedState("key", "value")
        assertEquals("value", vm.testGetSavedState("key"))
    }

    @Test
    fun `savedStateHandle returns null for missing key`() {
        val vm = TestViewModel()
        assertNull(vm.testGetSavedState("nonexistent"))
    }

    // ==================== UIEvent sealed class tests ====================

    @Test
    fun `UIEvent Toast data equality`() {
        val e1 = BaseViewModel.UIEvent.Toast("a")
        val e2 = BaseViewModel.UIEvent.Toast("a")
        assertEquals(e1, e2)
    }

    @Test
    fun `UIEvent Loading data equality`() {
        val e1 = BaseViewModel.UIEvent.Loading(true)
        val e2 = BaseViewModel.UIEvent.Loading(true)
        assertEquals(e1, e2)
    }

    @Test
    fun `UIEvent Navigate data equality`() {
        val e1 = BaseViewModel.UIEvent.Navigate("/home", mapOf("id" to 1))
        val e2 = BaseViewModel.UIEvent.Navigate("/home", mapOf("id" to 1))
        assertEquals(e1, e2)
    }

    @Test
    fun `UIEvent NavigateBack is singleton`() {
        val e1 = BaseViewModel.UIEvent.NavigateBack
        val e2 = BaseViewModel.UIEvent.NavigateBack
        assertSame(e1, e2)
    }
}
