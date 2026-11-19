package com.bookmate.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.ai.AiUsageRepository
import com.bookmate.app.domain.model.AccessType
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.usecase.GetDescriptionSummary
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Temporary test screen for the AI answer component. Replaced by the AI Chat tab in Step 4.6. */
@HiltViewModel
class AiTestViewModel @Inject constructor(
    private val getDescriptionSummary: GetDescriptionSummary,
    private val usage: AiUsageRepository
) : ViewModel() {

    private val controller = AiAnswerController(viewModelScope, usage) {
        FirebaseAuth.getInstance().currentUser?.uid
    }

    val state: StateFlow<AiAnswerState> = controller.state

    private val _remaining = MutableStateFlow<Int?>(null)
    val remaining: StateFlow<Int?> = _remaining.asStateFlow()

    init {
        viewModelScope.launch { controller.state.collect { refreshRemaining() } }
    }

    fun summarize() = controller.run { getDescriptionSummary(DEMO_BOOK) }

    fun retry() = controller.retry()

    fun useUpRequests() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            repeat(29) { usage.tryConsume(uid) }
            refreshRemaining()
        }
    }

    private suspend fun refreshRemaining() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        _remaining.value = uid?.let { usage.remainingToday(it) }
    }

    private companion object {
        val DEMO_BOOK = Book(
            id = "demo",
            title = "Dracula",
            author = "Bram Stoker",
            isbn = "",
            genre = "Horror",
            description = "A young solicitor travels to Transylvania to help Count Dracula buy a house " +
                "in England. He soon finds that he is a prisoner in the castle, and that the Count " +
                "is a vampire who plans to bring his curse to England.",
            coverUrl = null,
            accessType = AccessType.FREE,
            hasFullText = false,
            chapterCount = 0,
            searchKeywords = emptyList()
        )
    }
}