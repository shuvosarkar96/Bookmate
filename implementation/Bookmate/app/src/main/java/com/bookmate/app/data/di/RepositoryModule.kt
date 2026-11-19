package com.bookmate.app.data.di

import com.bookmate.app.data.repository.FirebaseAuthRepository
import com.bookmate.app.data.repository.FirebaseBookRepository
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.domain.repository.BookRepository
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
    abstract fun bindBookRepository(impl: FirebaseBookRepository): BookRepository
}