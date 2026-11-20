package com.bookmate.app.ui.mybooks

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

/**
 * Lets another screen (Profile) ask My Books to open on a given tab. The tab is taken once,
 * the next time My Books is shown.
 */
@Singleton
class MyBooksTabRequest @Inject constructor() {
    private val pending = MutableStateFlow<MyBooksTab?>(null)

    fun request(tab: MyBooksTab) {
        pending.value = tab
    }

    /** Returns the requested tab and forgets it. */
    fun consume(): MyBooksTab? = pending.getAndUpdate { null }
}