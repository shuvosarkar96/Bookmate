package com.bookmate.app.domain.ai

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Test

private class QuotaExceededException(message: String) : Exception(message)

class AiErrorMapperTest {

    @Test
    fun unknown_host_means_no_internet() {
        assertEquals(AiError.NO_INTERNET, AiErrorMapper.map(UnknownHostException("host")))
    }

    @Test
    fun socket_timeout_means_timeout_even_though_it_is_an_io_error() {
        assertEquals(AiError.TIMEOUT, AiErrorMapper.map(SocketTimeoutException("slow")))
    }

    @Test
    fun wrapped_cause_is_found() {
        val error = RuntimeException("call failed", IOException("network"))
        assertEquals(AiError.NO_INTERNET, AiErrorMapper.map(error))
    }

    @Test
    fun empty_answer_means_blocked() {
        assertEquals(AiError.BLOCKED, AiErrorMapper.map(EmptyAnswerException()))
    }

    @Test
    fun quota_exception_means_limit() {
        assertEquals(AiError.LIMIT, AiErrorMapper.map(QuotaExceededException("too many requests")))
    }

    @Test
    fun rejected_app_check_token_means_limit() {
        val error = Exception("Firebase App Check token is invalid.")
        assertEquals(AiError.LIMIT, AiErrorMapper.map(error))
    }

    @Test
    fun blocked_message_means_blocked() {
        assertEquals(AiError.BLOCKED, AiErrorMapper.map(Exception("Prompt was blocked for safety")))
    }

    @Test
    fun anything_else_is_other() {
        assertEquals(AiError.OTHER, AiErrorMapper.map(IllegalStateException("odd")))
    }

    @Test
    fun ai_exception_keeps_its_error() {
        assertEquals(AiError.TIMEOUT, AiErrorMapper.map(AiException(AiError.TIMEOUT)))
    }
}