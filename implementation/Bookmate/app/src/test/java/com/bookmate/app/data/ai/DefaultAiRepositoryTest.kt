package com.bookmate.app.data.ai

import com.bookmate.app.domain.ai.AiError
import com.bookmate.app.domain.ai.AiException
import com.bookmate.app.domain.ai.AiService
import java.net.UnknownHostException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

private class FakeAiService(
    private val generateResults: List<Result<String>> = emptyList(),
    private val streamFactories: List<() -> Flow<String>> = emptyList(),
    private val delayMillis: Long = 0
) : AiService {
    var generateCalls = 0
    var streamCalls = 0

    override suspend fun generate(prompt: String): Result<String> {
        val index = generateCalls++
        if (delayMillis > 0) delay(delayMillis)
        return generateResults[minOf(index, generateResults.lastIndex)]
    }

    override fun generateStream(prompt: String): Flow<String> {
        val index = streamCalls++
        return streamFactories[minOf(index, streamFactories.lastIndex)]()
    }
}

class DefaultAiRepositoryTest {

    private fun errorOf(result: Result<String>): AiError =
        (result.exceptionOrNull() as AiException).error

    @Test
    fun generate_returns_the_answer() = runBlocking {
        val service = FakeAiService(generateResults = listOf(Result.success("ok")))
        val result = DefaultAiRepository(service).generate("p")
        assertEquals("ok", result.getOrNull())
        assertEquals(1, service.generateCalls)
    }

    @Test
    fun generate_retries_once_after_an_unknown_error() = runBlocking {
        val service = FakeAiService(
            generateResults = listOf(Result.failure(RuntimeException("boom")), Result.success("ok"))
        )
        val result = DefaultAiRepository(service).generate("p")
        assertEquals("ok", result.getOrNull())
        assertEquals(2, service.generateCalls)
    }

    @Test
    fun generate_does_not_retry_a_limit_error() = runBlocking {
        val service = FakeAiService(generateResults = listOf(Result.failure(Exception("Quota exceeded"))))
        val result = DefaultAiRepository(service).generate("p")
        assertEquals(AiError.LIMIT, errorOf(result))
        assertEquals(1, service.generateCalls)
    }

    @Test
    fun generate_gives_up_after_one_retry() = runBlocking {
        val service = FakeAiService(
            generateResults = listOf(
                Result.failure(RuntimeException("one")),
                Result.failure(RuntimeException("two")),
                Result.success("never")
            )
        )
        val result = DefaultAiRepository(service).generate("p")
        assertEquals(AiError.OTHER, errorOf(result))
        assertEquals(2, service.generateCalls)
    }

    @Test
    fun generate_times_out() = runBlocking {
        val service = FakeAiService(generateResults = listOf(Result.success("late")), delayMillis = 500)
        val repository = DefaultAiRepository(service)
        repository.timeoutMillis = 50
        val result = repository.generate("p")
        assertEquals(AiError.TIMEOUT, errorOf(result))
        assertEquals(2, service.generateCalls)
    }

    @Test
    fun stream_emits_the_chunks() = runBlocking {
        val service = FakeAiService(streamFactories = listOf({ flowOf("a", "b") }))
        val chunks = DefaultAiRepository(service).generateStream("p").toList()
        assertEquals(listOf("a", "b"), chunks)
    }

    @Test
    fun stream_retries_when_it_fails_before_the_first_chunk() = runBlocking {
        val service = FakeAiService(
            streamFactories = listOf(
                { flow { throw RuntimeException("early") } },
                { flowOf("ok") }
            )
        )
        val chunks = DefaultAiRepository(service).generateStream("p").toList()
        assertEquals(listOf("ok"), chunks)
        assertEquals(2, service.streamCalls)
    }

    @Test
    fun stream_does_not_retry_after_a_chunk_was_shown() = runBlocking {
        val service = FakeAiService(
            streamFactories = listOf({ flow { emit("a"); delay(20); throw RuntimeException("late") } })
        )
        val received = mutableListOf<String>()
        try {
            DefaultAiRepository(service).generateStream("p").toList(received)
            fail("expected an AiException")
        } catch (e: AiException) {
            assertEquals(AiError.OTHER, e.error)
        }
        assertEquals(listOf("a"), received)
        assertEquals(1, service.streamCalls)
    }

    @Test
    fun stream_maps_no_internet_and_does_not_retry() = runBlocking {
        val service = FakeAiService(
            streamFactories = listOf({ flow { throw UnknownHostException("offline") } })
        )
        try {
            DefaultAiRepository(service).generateStream("p").toList()
            fail("expected an AiException")
        } catch (e: AiException) {
            assertTrue(e.error == AiError.NO_INTERNET)
        }
        assertEquals(1, service.streamCalls)
    }
}