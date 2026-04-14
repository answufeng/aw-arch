package com.answufeng.arch.config

import org.junit.Assert.*
import org.junit.Test

class AwArchTest {

    @Test
    fun `default logger is DefaultAwLogger`() {
        val arch = AwArch
        assertNotNull(arch.logger)
        assertTrue(arch.logger is DefaultAwLogger)
    }

    @Test
    fun `init block configures logger`() {
        val originalLogger = AwArch.logger
        val customLogger = object : AwLogger {
            override fun d(tag: String, message: String) {}
            override fun w(tag: String, message: String, throwable: Throwable?) {}
            override fun e(tag: String, message: String, throwable: Throwable?) {}
        }
        AwArch.init {
            logger = customLogger
        }
        assertSame(customLogger, AwArch.logger)

        AwArch.init {
            logger = originalLogger
        }
    }

    @Test
    fun `DefaultAwLogger does not crash on debug`() {
        val logger = DefaultAwLogger()
        logger.d("TestTag", "test message")
    }

    @Test
    fun `DefaultAwLogger does not crash on warning`() {
        val logger = DefaultAwLogger()
        logger.w("TestTag", "warning message")
        logger.w("TestTag", "warning with throwable", RuntimeException("test"))
    }

    @Test
    fun `DefaultAwLogger does not crash on error`() {
        val logger = DefaultAwLogger()
        logger.e("TestTag", "error message")
        logger.e("TestTag", "error with throwable", RuntimeException("test"))
    }
}
