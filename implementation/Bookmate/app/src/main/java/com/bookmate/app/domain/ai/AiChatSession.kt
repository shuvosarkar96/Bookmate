package com.bookmate.app.domain.ai

import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.ReadingProgress
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update

/** What the Book Chat can be asked to do. */
sealed interface ChatAction {
    data object Summary : ChatAction
    data object Recap : ChatAction
    data object Explain : ChatAction
    data class Question(val text: String) : ChatAction
}

/** The book and the reader position the chat works at. The position decides what text may be sent. */
data class ChatContext(
    val book: Book,
    val chapter: Int,
    val paragraph: Int,
    /** True when only the book description may be used: no readable full text. */
    val descriptionOnly: Boolean = false
) {
    /** A chat opened from Book Detail has no reading position (chapter 0). */
    val isBookLevel: Boolean get() = chapter == 0

    companion object {
        /**
         * A book picked in the chat. With full text the Member may read and a saved reading position,
         * the chat starts at that position. Otherwise it starts at book level.
         */
        fun forPick(book: Book, canRead: Boolean, progress: ReadingProgress?): ChatContext =
            if (canRead && book.hasFullText && progress != null && progress.chapter >= 1) {
                ChatContext(book, progress.chapter, progress.paragraph)
            } else {
                forBook(book, canRead)
            }

        fun forBook(book: Book, canRead: Boolean) = ChatContext(
            book = book,
            chapter = 0,
            paragraph = 0,
            descriptionOnly = !(canRead && book.hasFullText)
        )
    }
}

enum class ChatRole { USER, AI }

data class ChatMessage(
    val role: ChatRole,
    val text: String = "",
    /** For a user message: what was asked. */
    val action: ChatAction? = null
)

/**
 * In-memory chat shared by the Reader and the AI tab. Messages are not stored anywhere else.
 * Opening a different book clears the messages.
 */
@Singleton
class AiChatSession @Inject constructor() {
    private val _context = MutableStateFlow<ChatContext?>(null)
    val context: StateFlow<ChatContext?> = _context.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _pending = MutableStateFlow<ChatAction?>(null)
    val pending: StateFlow<ChatAction?> = _pending.asStateFlow()

    fun open(context: ChatContext, action: ChatAction?) {
        if (_context.value?.book?.id != context.book.id) _messages.value = emptyList()
        _context.value = context
        _pending.value = action
    }

    /** Takes the waiting action, once. */
    fun consumePending(): ChatAction? = _pending.getAndUpdate { null }

    fun addMessage(message: ChatMessage) {
        _messages.update { it + message }
    }

    fun clear() {
        _context.value = null
        _messages.value = emptyList()
        _pending.value = null
    }
}