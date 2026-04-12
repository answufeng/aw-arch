package com.answufeng.arch.nav

import org.junit.Assert.*
import org.junit.Test

class BrickNavTest {

    @Test
    fun `NavAnim NONE resolves to null`() {
        assertNull(NavAnim.NONE.resolve())
    }

    @Test
    fun `NavAnim FADE resolves to non-null AnimSet`() {
        val animSet = NavAnim.FADE.resolve()
        assertNotNull(animSet)
        assertTrue(animSet!!.enter != 0)
        assertTrue(animSet.exit != 0)
    }

    @Test
    fun `NavAnim SLIDE_HORIZONTAL resolves to non-null AnimSet`() {
        val animSet = NavAnim.SLIDE_HORIZONTAL.resolve()
        assertNotNull(animSet)
        assertTrue(animSet!!.enter != 0)
        assertTrue(animSet.exit != 0)
    }

    @Test
    fun `NavAnim SLIDE_VERTICAL resolves to non-null AnimSet`() {
        val animSet = NavAnim.SLIDE_VERTICAL.resolve()
        assertNotNull(animSet)
        assertTrue(animSet!!.enter != 0)
        assertTrue(animSet.exit != 0)
    }

    @Test
    fun `NavOptions default values`() {
        val options = NavOptions()
        assertTrue(options.addToBackStack)
        assertFalse(options.singleTop)
        assertEquals(NavAnim.SLIDE_HORIZONTAL, options.anim)
    }

    @Test
    fun `NavOptions custom animation overrides preset`() {
        val options = NavOptions().apply {
            setCustomAnim(
                enter = 1,
                exit = 2,
                popEnter = 3,
                popExit = 4,
            )
        }
        assertEquals(NavAnim.NONE, options.anim)
        val animSet = options.resolveAnimSet()
        assertNotNull(animSet)
        assertEquals(1, animSet!!.enter)
        assertEquals(2, animSet.exit)
        assertEquals(3, animSet.popEnter)
        assertEquals(4, animSet.popExit)
    }

    @Test
    fun `NavOptions resolveAnimSet returns null for NONE with no custom`() {
        val options = NavOptions().apply { anim = NavAnim.NONE }
        assertNull(options.resolveAnimSet())
    }

    @Test
    fun `NavInterceptor functional interface works`() {
        var intercepted = false
        val interceptor = NavInterceptor { _, _, _ ->
            intercepted = true
            true
        }
        val result = interceptor.onNavigate("from", "to", null)
        assertTrue(intercepted)
        assertTrue(result)
    }

    @Test
    fun `NavInterceptor can block navigation`() {
        val interceptor = NavInterceptor { _, to, _ ->
            to != "blocked"
        }
        assertTrue(interceptor.onNavigate(null, "allowed", null))
        assertFalse(interceptor.onNavigate(null, "blocked", null))
    }
}
