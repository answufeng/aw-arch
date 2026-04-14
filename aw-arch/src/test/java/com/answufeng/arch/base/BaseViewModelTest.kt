package com.answufeng.arch.base

import com.answufeng.arch.test.BrickTestRule
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
    val brickTestRule = BrickTestRule()

    class TestMvvmViewModel(savedStateHandle: androidx.lifecycle.SavedStateHandle? = null) :
        MvvmViewModel(savedStateHandle) {

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

    @Test
    fun `launch executes block`() = runTest {
        val vm = TestMvvmViewModel()
        var executed = false
        vm.testLaunch { executed = true }
        advanceUntilIdle()
        assertTrue(executed)
    }

    @Test
    fun `launch handles exception via handleException`() = runTest {
        val vm = TestMvvmViewModel()
        vm.testLaunch { throw RuntimeException("test") }
        advanceUntilIdle()
        assertNotNull(vm.exceptionHandled)
        assertEquals("test", vm.exceptionHandled?.message)
    }

    @Test
    fun `launch with onError callback`() = runTest {
        val vm = TestMvvmViewModel()
        vm.testLaunchWithError { throw RuntimeException("custom") }
        advanceUntilIdle()
        assertNotNull(vm.exceptionHandled)
        assertEquals("custom", vm.exceptionHandled?.message)
    }

    @Test
    fun `launchIO handles exception`() = runTest {
        val vm = TestMvvmViewModel()
        vm.testLaunchIO { throw RuntimeException("io error") }
        advanceUntilIdle()
        assertNotNull(vm.exceptionHandled)
        assertEquals("io error", vm.exceptionHandled?.message)
    }

    @Test
    fun `launchDefault handles exception`() = runTest {
        val vm = TestMvvmViewModel()
        vm.testLaunchDefault { throw RuntimeException("default error") }
        val deadline = System.currentTimeMillis() + 3000L
        while (vm.exceptionHandled == null && System.currentTimeMillis() < deadline) {
            Thread.sleep(50)
        }
        assertNotNull(vm.exceptionHandled)
        assertEquals("default error", vm.exceptionHandled?.message)
    }

    @Test
    fun `showToast sends Toast event`() = runTest {
        val vm = TestMvvmViewModel()
        vm.testShowToast("hello")
        val event = vm.uiEvent.first()
        assertTrue(event is MvvmViewModel.UIEvent.Toast)
        assertEquals("hello", (event as MvvmViewModel.UIEvent.Toast).message)
    }

    @Test
    fun `showLoading sends Loading event`() = runTest {
        val vm = TestMvvmViewModel()
        vm.testShowLoading(true)
        val event = vm.uiEvent.first()
        assertTrue(event is MvvmViewModel.UIEvent.Loading)
        assertTrue((event as MvvmViewModel.UIEvent.Loading).show)
    }

    @Test
    fun `navigate sends Navigate event`() = runTest {
        val vm = TestMvvmViewModel()
        vm.testNavigate("/detail")
        val event = vm.uiEvent.first()
        assertTrue(event is MvvmViewModel.UIEvent.Navigate)
        assertEquals("/detail", (event as MvvmViewModel.UIEvent.Navigate).route)
    }

    @Test
    fun `navigateBack sends NavigateBack event`() = runTest {
        val vm = TestMvvmViewModel()
        vm.testNavigateBack()
        val event = vm.uiEvent.first()
        assertTrue(event is MvvmViewModel.UIEvent.NavigateBack)
    }

    @Test
    fun `sendEvent sends Custom event`() = runTest {
        val vm = TestMvvmViewModel()
        vm.testSendEvent(MvvmViewModel.UIEvent.Custom("key", "data"))
        val event = vm.uiEvent.first()
        assertTrue(event is MvvmViewModel.UIEvent.Custom)
        assertEquals("key", (event as MvvmViewModel.UIEvent.Custom).key)
        assertEquals("data", event.data)
    }

    @Test
    fun `savedStateHandle get and set`() {
        val handle = androidx.lifecycle.SavedStateHandle()
        val vm = TestMvvmViewModel(handle)
        vm.testSetSavedState("key", "value")
        assertEquals("value", vm.testGetSavedState("key"))
    }

    @Test
    fun `savedStateHandle returns null for missing key`() {
        val vm = TestMvvmViewModel()
        assertNull(vm.testGetSavedState("nonexistent"))
    }

    @Test
    fun `UIEvent Toast data equality`() {
        val e1 = MvvmViewModel.UIEvent.Toast("a")
        val e2 = MvvmViewModel.UIEvent.Toast("a")
        assertEquals(e1, e2)
    }

    @Test
    fun `UIEvent Loading data equality`() {
        val e1 = MvvmViewModel.UIEvent.Loading(true)
        val e2 = MvvmViewModel.UIEvent.Loading(true)
        assertEquals(e1, e2)
    }

    @Test
    fun `UIEvent Navigate data equality`() {
        val e1 = MvvmViewModel.UIEvent.Navigate("/home", mapOf("id" to 1))
        val e2 = MvvmViewModel.UIEvent.Navigate("/home", mapOf("id" to 1))
        assertEquals(e1, e2)
    }

    @Test
    fun `UIEvent NavigateBack is singleton`() {
        val e1 = MvvmViewModel.UIEvent.NavigateBack
        val e2 = MvvmViewModel.UIEvent.NavigateBack
        assertSame(e1, e2)
    }
}
