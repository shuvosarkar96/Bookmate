package com.bookmate.app.util

import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book

/** A Member can read a Free book, or a Premium book with an active subscription. */
fun canReadBook(book: Book, hasActiveSubscription: Boolean): Boolean =
    book.accessType == AccessType.FREE || hasActiveSubscription