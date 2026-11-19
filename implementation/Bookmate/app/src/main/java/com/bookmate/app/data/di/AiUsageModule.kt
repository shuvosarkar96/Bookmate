package com.bookmate.app.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AiUsageStore

private val Context.aiUsageDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_usage")

@Module
@InstallIn(SingletonComponent::class)
object AiUsageModule {

    @Provides
    @Singleton
    @AiUsageStore
    fun provideAiUsageStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.aiUsageDataStore
}