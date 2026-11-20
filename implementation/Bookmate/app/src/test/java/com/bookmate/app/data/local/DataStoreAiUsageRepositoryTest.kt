package com.bookmate.app.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreAiUsageRepositoryTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun repository(): DataStoreAiUsageRepository {
        val store = PreferenceDataStoreFactory.create(scope = scope) {
            File(folder.root, "usage.preferences_pb")
        }
        return DataStoreAiUsageRepository(store).also { it.today = { "2026-11-19" } }
    }

    @Test
    fun allows_30_requests_and_refuses_the_31st() = runBlocking {
        val repo = repository()
        repeat(30) { assertTrue(repo.tryConsume("u1")) }
        assertFalse(repo.tryConsume("u1"))
        assertEquals(0, repo.remainingToday("u1"))
    }

    @Test
    fun remaining_goes_down_with_each_request() = runBlocking {
        val repo = repository()
        assertEquals(30, repo.remainingToday("u1"))
        repeat(3) { repo.tryConsume("u1") }
        assertEquals(27, repo.remainingToday("u1"))
    }

    @Test
    fun each_user_has_their_own_count() = runBlocking {
        val repo = repository()
        repeat(30) { repo.tryConsume("u1") }
        assertFalse(repo.tryConsume("u1"))
        assertTrue(repo.tryConsume("u2"))
        assertEquals(29, repo.remainingToday("u2"))
    }

    @Test
    fun a_new_day_starts_again_at_zero() = runBlocking {
        val repo = repository()
        repeat(30) { repo.tryConsume("u1") }
        assertFalse(repo.tryConsume("u1"))
        repo.today = { "2026-11-20" }
        assertEquals(30, repo.remainingToday("u1"))
        assertTrue(repo.tryConsume("u1"))
        assertEquals(29, repo.remainingToday("u1"))
    }
}