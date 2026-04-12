package com.answufeng.arch.config

import org.junit.Assert.*
import org.junit.Test

class BrickArchTest {

    @Test
    fun `default logger is DefaultBrickLogger`() {
        val arch = BrickArch
        assertNotNull(arch.logger)
        assertTrue(arch.logger is DefaultBrickLogger)
    }

    @Test
    fun `init block configures logger`() {
        val originalLogger = BrickArch.logger
        val customLogger = object : BrickLogger {
            override fun d(tag: String, message: String) {}
            override fun w(tag: String, message: String, throwable: Throwable?) {}
            override fun e(tag: String, message: String, throwable: Throwable?) {}
        }
        BrickArch.init {
            logger = customLogger
        }
        assertSame(customLogger, BrickArch.logger)

        BrickArch.init {
            logger = originalLogger
        }
    }

    @Test
    fun `DefaultBrickLogger does not crash on debug`() {
        val logger = DefaultBrickLogger()
        logger.d("TestTag", "test message")
    }

    @Test
    fun `DefaultBrickLogger does not crash on warning`() {
        val logger = DefaultBrickLogger()
        logger.w("TestTag", "warning message")
        logger.w("TestTag", "warning with throwable", RuntimeException("test"))
    }

    @Test
    fun `DefaultBrickLogger does not crash on error`() {
        val logger = DefaultBrickLogger()
        logger.e("TestTag", "error message")
        logger.e("TestTag", "error with throwable", RuntimeException("test"))
    }
}
