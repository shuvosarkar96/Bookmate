package com.bookmate.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.ai.AiChatSession
import com.bookmate.app.domain.ai.AiUsageRepository
import com.bookmate.app.domain.ai.ChatAction
import com.bookmate.app.domain.ai.ChatContext
import com.bookmate.app.domain.ai.ChatMessage
import com.bookmate.app.domain.ai.ChatRole
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.ReadingEntry
import com.bookmate.app.domain.model.ReadingProgress
import com.bookmate.app.domain.repository.SubscriptionRepository
import com.bookmate.app.domain.usecase.AskAboutBook
import com.bookmate.app.domain.usecase.ExplainPart
import com.bookmate.app.domain.usecase.GetBookSummary
import com.bookmate.app.domain.usecase.GetChapterSummary
import com.bookmate.app.domain.usecase.GetDescriptionSummary
import com.bookmate.app.domain.usecase.GetMyBooks
import com.bookmate.app.domain.usecase.GetRecap
import com.bookmate.app.util.canReadBook
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The books the Member can pick when no book is selected. */
data class PickerState(
    val isLoading: Boolean = true,
    val reading: List<ReadingEntry> = emptyList(),
    val saved: List<Book> = emptyList(),
    val hasActive: Boolean = false
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val session: AiChatSession,
    private val getBookSummary: GetBookSummary,
    private val getDescriptionSummary: GetDescriptionSummary,
    private val getChapterSummary: GetChapterSummary,
    private val getRecap: GetRecap,
    private val explainPart: ExplainPart,
    private val askAboutBook: AskAboutBook,
    private val getMyBooks: GetMyBooks,
    private val subscriptionRepository: SubscriptionRepository,
    aiUsageRepository: AiUsageRepository
) : ViewModel() {

    private val controller = AiAnswerController(viewModelScope, aiUsageRepository) {
        FirebaseAuth.getInstance().currentUser?.uid
    }

    val context: StateFlow<ChatContext?> = session.context
    val messages: StateFlow<List<ChatMessage>> = session.messages
    val answer: StateFlow<AiAnswerState> = controller.state

    init {
        // A finished answer becomes a message in the chat.
        viewModelScope.launch {
            controller.state.collect { state ->
                if (state is AiAnswerState.Done) {
                    session.addMessage(ChatMessage(ChatRole.AI, state.text))
                    controller.reset()
                }
            }
        }
        // An action handed over by the Reader sheet.
        viewModelScope.launch {
            session.pending.filterNotNull().collect {
                session.consumePending()?.let { action -> send(action) }
            }
        }
    }

    fun send(action: ChatAction) {
        val chat = session.context.value ?: return
        if (controller.state.value.isBusy) return
        if (action is ChatAction.Question && action.text.isBlank()) return
        // Without a reading position only the summary is possible.
        if (chat.isBookLevel && action != ChatAction.Summary) return
        session.addMessage(
            ChatMessage(
                role = ChatRole.USER,
                text = (action as? ChatAction.Question)?.text.orEmpty(),
                action = action
            )
        )
        val book = chat.book
        val chapter = chat.chapter
        val position = chat.paragraph
        when (action) {
            ChatAction.Summary -> when {
                chat.descriptionOnly -> controller.run { getDescriptionSummary(book) }
                chat.isBookLevel -> controller.run { getBookSummary(book) }
                else -> controller.run { getChapterSummary(book, chapter) }
            }
            ChatAction.Recap -> controller.run { getRecap(book, chapter, position) }
            ChatAction.Explain -> controller.run { explainPart(book, chapter, position) }
            is ChatAction.Question -> controller.run { askAboutBook(book, chapter, position, action.text) }
        }
    }

    fun retry() = controller.retry()

    private val _picker = MutableStateFlow(PickerState())
    val picker: StateFlow<PickerState> = _picker.asStateFlow()

    /** Loads the Reading and Saved books for the picker. */
    fun loadPicker() {
        viewModelScope.launch {
            _picker.update { it.copy(isLoading = true) }
            val hasActive = subscriptionRepository.getMySubscription().getOrNull()?.isActive() == true
            val books = getMyBooks().getOrNull()
            _picker.value = PickerState(
                isLoading = false,
                reading = books?.reading.orEmpty(),
                saved = books?.saved.orEmpty(),
                hasActive = hasActive
            )
        }
    }

    /** Starts the chat with this book. [progress] is the saved reading position, if there is one. */
    fun pick(book: Book, progress: ReadingProgress?) {
        val canRead = canReadBook(book, _picker.value.hasActive)
        session.open(ChatContext.forPick(book, canRead, progress), null)
    }
}