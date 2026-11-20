package com.bookmate.app.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.bookmate.app.data.di.AiUsageStore
import com.bookmate.app.domain.ai.AiUsageRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class DataStoreAiUsageRepository @Inject constructor(
    @AiUsageStore private val store: DataStore<Preferences>
) : AiUsageRepository {

    /** Today's date as text. A var only so that unit tests can change the day. */
    internal var today: () -> String = { LocalDate.now().toString() }

    override suspend fun tryConsume(userId: String): Boolean {
        var allowed = false
        store.edit { prefs ->
            val day = today()
            // Forget the counters of earlier days.
            prefs.asMap().keys
                .filter { it.name.startsWith(PREFIX) && !it.name.endsWith("_$day") }
                .forEach { prefs.remove(it) }
            val key = keyFor(userId, day)
            val used = prefs[key] ?: 0
            if (used < AiUsageRepository.DAILY_LIMIT) {
                prefs[key] = used + 1
                allowed = true
            }
        }
        return allowed
    }

    override suspend fun remainingToday(userId: String): Int {
        val used = store.data.first()[keyFor(userId, today())] ?: 0
        return (AiUsageRepository.DAILY_LIMIT - used).coerceAtLeast(0)
    }

    private fun keyFor(userId: String, day: String) = intPreferencesKey("$PREFIX${userId}_$day")

    private companion object {
        const val PREFIX = "ai_count_"
    }
}