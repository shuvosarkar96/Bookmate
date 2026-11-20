package com.bookmate.app.domain.usecase

import com.bookmate.app.domain.model.MyBooks
import com.bookmate.app.domain.repository.BookRepository
import com.bookmate.app.domain.repository.ReadingProgressRepository
import com.bookmate.app.domain.repository.SavedBooksRepository
import com.bookmate.app.util.buildMyBooks
import javax.inject.Inject

/** Loads the Reading, Saved and Finished lists of the signed-in Member. Fails if any load fails. */
class GetMyBooks @Inject constructor(
    private val bookRepository: BookRepository,
    private val readingProgressRepository: ReadingProgressRepository,
    private val savedBooksRepository: SavedBooksRepository
) {
    suspend operator fun invoke(): Result<MyBooks> {
        val catalog = bookRepository.getBooks().getOrElse { return Result.failure(it) }
        val progress = readingProgressRepository.getAllProgress().getOrElse { return Result.failure(it) }
        val savedIds = savedBooksRepository.getSavedBookIds().getOrElse { return Result.failure(it) }
        return Result.success(buildMyBooks(catalog, progress, savedIds))
    }
}