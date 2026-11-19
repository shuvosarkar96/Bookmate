package com.bookmate.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bookmate.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val textSizeKey = intPreferencesKey("reading_text_size")

    override val readingTextSize: Flow<Int> =
        context.settingsDataStore.data.map { it[textSizeKey] ?: SettingsRepository.DEFAULT_TEXT_SIZE }

    override suspend fun setReadingTextSize(sizeSp: Int) {
        context.settingsDataStore.edit { it[textSizeKey] = sizeSp }
    }
}