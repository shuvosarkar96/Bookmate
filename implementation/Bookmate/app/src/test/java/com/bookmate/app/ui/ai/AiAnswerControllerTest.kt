package com.bookmate.app.ui.ai

import com.bookmate.app.domain.ai.AiError
import com.bookmate.app.domain.ai.AiException
import com.bookmate.app.domain.ai.AiUsageRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

private class CountingUsage(private var left: Int = 30) : AiUsageRepository {
    var consumed = 0

    override suspend fun tryConsume(userId: String): Boolean {
        if (left <= 0) return false
        left--
        consumed++
        return true
    }

    override suspend fun remainingToday(userId: String): Int = left
}

class AiAnswerControllerTest {

    // Unconfined: the request starts at once, so the state can be checked right after run().
    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private fun controller(usage: AiUsageRepository = CountingUsage(), user: String? = "u1") =
        AiAnswerController(scope, usage) { user }

    @Test
    fun streams_the_text_and_ends_done() {
        val usage = CountingUsage()
        val controller = controller(usage)
        controller.run { flowOf("Hel", "lo") }
        assertEquals(AiAnswerState.Done("Hello"), controller.state.value)
        assertEquals(1, usage.consumed)
    }

    @Test
    fun at_the_daily_cap_it_shows_the_limit_error_and_sends_no_request() {
        var called = false
        val controller = controller(CountingUsage(left = 0))
        controller.run { called = true; flowOf("x") }
        assertEquals(AiAnswerState.Error(AiError.LIMIT), controller.state.value)
        assertFalse(called)
    }

    @Test
    fun a_failure_becomes_a_mapped_error() {
        val controller = controller()
        controller.run { flow<String> { throw AiException(AiError.TIMEOUT) } }
        assertEquals(AiAnswerState.Error(AiError.TIMEOUT), controller.state.value)
    }

    @Test
    fun an_empty_answer_is_blocked() {
        val controller = controller()
        controller.run { flowOf() }
        assertEquals(AiAnswerState.Error(AiError.BLOCKED), controller.state.value)
    }

    @Test
    fun retry_repeats_the_last_request() {
        val usage = CountingUsage()
        val controller = controller(usage)
        var calls = 0
        controller.run {
            calls++
            if (calls == 1) flow<String> { throw AiException(AiError.TIMEOUT) } else flowOf("ok")
        }
        assertEquals(AiAnswerState.Error(AiError.TIMEOUT), controller.state.value)
        controller.retry()
        assertEquals(AiAnswerState.Done("ok"), controller.state.value)
        assertEquals(2, calls)
        assertEquals(2, usage.consumed)
    }

    @Test
    fun a_second_request_is_ignored_while_one_is_running() {
        val gate = CompletableDeferred<Unit>()
        var secondCalled = false
        val controller = controller()
        controller.run { flow { emit("a"); gate.await(); emit("b") } }
        assertEquals(AiAnswerState.Streaming("a"), controller.state.value)
        controller.run { secondCalled = true; flowOf("x") }
        assertFalse(secondCalled)
        gate.complete(Unit)
        assertEquals(AiAnswerState.Done("ab"), controller.state.value)
    }

    @Test
    fun a_broken_stream_keeps_the_text_that_arrived() {
        val controller = controller()
        controller.run { flow { emit("abc"); throw AiException(AiError.TIMEOUT) } }
        assertEquals(AiAnswerState.Error(AiError.TIMEOUT, "abc"), controller.state.value)
    }

    @Test
    fun without_a_user_it_shows_an_error_and_counts_nothing() {
        val usage = CountingUsage()
        val controller = controller(usage, user = null)
        controller.run { flowOf("x") }
        assertEquals(AiAnswerState.Error(AiError.OTHER), controller.state.value)
        assertEquals(0, usage.consumed)
    }
}