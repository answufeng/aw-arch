package com.answufeng.arch.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LoadStateTest {

    @Test
    fun `Loading state has no data`() {
        val state: LoadState<Int> = LoadState.Loading
        assertFalse(state is LoadState.Success)
        assertFalse(state is LoadState.Error)
    }

    @Test
    fun `Success state holds data`() {
        val state = LoadState.Success(42)
        assertEquals(42, state.data)
    }

    @Test
    fun `Error state holds exception and message`() {
        val error = RuntimeException("test error")
        val state = LoadState.Error(error, "test error")
        assertEquals(error, state.exception)
        assertEquals("test error", state.message)
    }

    @Test
    fun `Error state with default message`() {
        val error = RuntimeException("boom")
        val state = LoadState.Error(error)
        assertEquals("boom", state.message)
    }

    @Test
    fun `getOrNull returns data for Success`() {
        val state = LoadState.Success("hello")
        assertEquals("hello", state.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Loading`() {
        val state: LoadState<String> = LoadState.Loading
        assertNull(state.getOrNull())
    }

    @Test
    fun `getOrNull returns null for Error`() {
        val state: LoadState<String> = LoadState.Error(RuntimeException())
        assertNull(state.getOrNull())
    }

    @Test
    fun `getOrDefault returns data for Success`() {
        val state = LoadState.Success(10)
        assertEquals(10, state.getOrDefault(0))
    }

    @Test
    fun `getOrDefault returns default for Loading`() {
        val state: LoadState<Int> = LoadState.Loading
        assertEquals(0, state.getOrDefault(0))
    }

    @Test
    fun `map transforms Success data`() {
        val state = LoadState.Success(5)
        val mapped = state.map { it * 2 }
        assertEquals(LoadState.Success(10), mapped)
    }

    @Test
    fun `map preserves Loading`() {
        val state: LoadState<Int> = LoadState.Loading
        val mapped = state.map { it * 2 }
        assertEquals(LoadState.Loading, mapped)
    }

    @Test
    fun `map preserves Error`() {
        val error = RuntimeException("fail")
        val state: LoadState<Int> = LoadState.Error(error, "fail")
        val mapped = state.map { it * 2 }
        assertTrue(mapped is LoadState.Error)
        assertEquals("fail", (mapped as LoadState.Error).message)
    }

    @Test
    fun `recover returns data for Success`() {
        val state = LoadState.Success(42)
        val recovered = state.recover(0)
        assertEquals(LoadState.Success(42), recovered)
    }

    @Test
    fun `recover returns default for Error`() {
        val state: LoadState<Int> = LoadState.Error(RuntimeException())
        assertEquals(LoadState.Success(0), state.recover(0))
    }

    @Test
    fun `recoverWith transforms Error`() {
        val state: LoadState<Int> = LoadState.Error(RuntimeException("fail"))
        val recovered = state.recoverWith { -1 }
        assertEquals(LoadState.Success(-1), recovered)
    }

    @Test
    fun `recoverWith preserves Success`() {
        val state = LoadState.Success(42)
        val recovered = state.recoverWith { -1 }
        assertEquals(LoadState.Success(42), recovered)
    }

    @Test
    fun `combine Loading and Loading returns Loading`() {
        val a: LoadState<Int> = LoadState.Loading
        val b: LoadState<String> = LoadState.Loading
        val result = a.combine(b)
        assertEquals(LoadState.Loading, result)
    }

    @Test
    fun `combine Loading and Success returns Loading`() {
        val a: LoadState<Int> = LoadState.Loading
        val b = LoadState.Success("hello")
        val result = a.combine(b)
        assertEquals(LoadState.Loading, result)
    }

    @Test
    fun `combine Success and Success returns paired Success`() {
        val a = LoadState.Success(1)
        val b = LoadState.Success("two")
        val result = a.combine(b)
        assertEquals(LoadState.Success(1 to "two"), result)
    }

    @Test
    fun `combine Error and Success returns Error`() {
        val error = RuntimeException("fail")
        val a: LoadState<Int> = LoadState.Error(error, "fail")
        val b = LoadState.Success("hello")
        val result = a.combine(b)
        assertTrue(result is LoadState.Error)
        assertEquals("fail", (result as LoadState.Error).message)
    }

    @Test
    fun `fold calls onLoading`() {
        val state: LoadState<Int> = LoadState.Loading
        var loadingCalled = false
        state.fold(
            onLoading = { loadingCalled = true },
            onSuccess = {},
            onError = {}
        )
        assertTrue(loadingCalled)
    }

    @Test
    fun `fold calls onSuccess`() {
        val state = LoadState.Success(42)
        var result = 0
        state.fold(
            onLoading = {},
            onSuccess = { result = it },
            onError = {}
        )
        assertEquals(42, result)
    }

    @Test
    fun `fold calls onError`() {
        val state: LoadState<Int> = LoadState.Error(RuntimeException("fail"), "fail")
        var errorMsg = ""
        state.fold(
            onLoading = {},
            onSuccess = {},
            onError = { errorMsg = it.message ?: "" }
        )
        assertEquals("fail", errorMsg)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `asLoadState converts Flow to LoadState Flow`() = runTest {
        val source = flowOf(1)
        val loadStateFlow = source.asLoadState()
        val results = mutableListOf<LoadState<Int>>()
        loadStateFlow.collect { results.add(it) }
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it is LoadState.Success })
        assertEquals(1, (results.first { it is LoadState.Success } as LoadState.Success).data)
    }

    @Test
    fun `mapLoadState transforms inner data`() = runTest {
        val source = flowOf(LoadState.Success(5))
        val mapped = source.mapLoadState { it * 3 }
        val result = mapped.first()
        assertEquals(LoadState.Success(15), result)
    }

    @Test
    fun `loadStateCatching catches exception`() = runTest {
        val result = loadStateCatching { throw RuntimeException("fail") }
        assertTrue(result is LoadState.Error)
        assertEquals("fail", (result as LoadState.Error).message)
    }

    @Test
    fun `loadStateCatching returns Success on success`() = runTest {
        val result = loadStateCatching { 42 }
        assertEquals(LoadState.Success(42), result)
    }

    @Test
    fun `isLoading isSuccess isError properties`() {
        assertTrue(LoadState.Loading.isLoading)
        assertFalse(LoadState.Loading.isSuccess)
        assertFalse(LoadState.Loading.isError)

        assertFalse(LoadState.Success(1).isLoading)
        assertTrue(LoadState.Success(1).isSuccess)
        assertFalse(LoadState.Success(1).isError)

        assertFalse(LoadState.Error(RuntimeException()).isLoading)
        assertFalse(LoadState.Error(RuntimeException()).isSuccess)
        assertTrue(LoadState.Error(RuntimeException()).isError)
    }

    @Test
    fun `onSuccess callback fires for Success`() {
        var called = false
        LoadState.Success(42).onSuccess { called = true }
        assertTrue(called)
    }

    @Test
    fun `onError callback fires for Error`() {
        var called = false
        LoadState.Error(RuntimeException()).onError { called = true }
        assertTrue(called)
    }

    @Test
    fun `onLoading callback fires for Loading`() {
        var called = false
        LoadState.Loading.onLoading { called = true }
        assertTrue(called)
    }
}
