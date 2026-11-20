package com.bookmate.app.data.di

import com.bookmate.app.data.repository.FirebaseAuthRepository
import com.bookmate.app.data.repository.FirestoreAdminRepository
import com.bookmate.app.data.repository.FirestoreNotificationRepository
import com.bookmate.app.data.repository.FirebaseBookRepository
import com.bookmate.app.data.repository.FirestoreSavedBooksRepository
import com.bookmate.app.data.local.DataStoreSettingsRepository
import com.bookmate.app.data.ai.DefaultAiRepository
import com.bookmate.app.data.local.DataStoreAiUsageRepository
import com.bookmate.app.data.ai.FirebaseAiService
import com.bookmate.app.data.repository.FirestoreChapterRepository
import com.bookmate.app.data.repository.FirestoreReadingProgressRepository
import com.bookmate.app.data.repository.FirestoreSubscriptionRepository
import com.bookmate.app.domain.repository.AdminRepository
import com.bookmate.app.domain.repository.NotificationRepository
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.domain.repository.SavedBooksRepository
import com.bookmate.app.domain.repository.ChapterRepository
import com.bookmate.app.domain.repository.ReadingProgressRepository
import com.bookmate.app.domain.repository.SettingsRepository
import com.bookmate.app.domain.ai.AiRepository
import com.bookmate.app.domain.ai.AiUsageRepository
import com.bookmate.app.domain.ai.AiService
import com.bookmate.app.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindAdminRepository(impl: FirestoreAdminRepository): AdminRepository

    @Binds
    abstract fun bindNotificationRepository(impl: FirestoreNotificationRepository): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindBookRepository(impl: FirebaseBookRepository): BookRepository

    @Binds
    @Singleton
    abstract fun bindSavedBooksRepository(impl: FirestoreSavedBooksRepository): SavedBooksRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: FirestoreSubscriptionRepository): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindChapterRepository(impl: FirestoreChapterRepository): ChapterRepository

    @Binds
    @Singleton
    abstract fun bindReadingProgressRepository(impl: FirestoreReadingProgressRepository): ReadingProgressRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAiService(impl: FirebaseAiService): AiService

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: DefaultAiRepository): AiRepository

    @Binds
    @Singleton
    abstract fun bindAiUsageRepository(impl: DataStoreAiUsageRepository): AiUsageRepository
}