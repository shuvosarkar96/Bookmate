package com.bookmate.app.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bookmate.app.domain.ai.AiChatSession
import com.bookmate.app.domain.ai.AiUsageRepository
import com.bookmate.app.domain.ai.ChatAction
import com.bookmate.app.domain.ai.ChatContext
import com.bookmate.app.domain.ai.ChatMessage
import com.bookmate.app.domain.ai.ChatRole
import com.bookmate.app.domain.usecase.AskAboutBook
import com.bookmate.app.domain.usecase.ExplainPart
import com.bookmate.app.domain.usecase.GetBookSummary
import com.bookmate.app.domain.usecase.GetChapterSummary
import com.bookmate.app.domain.usecase.GetDescriptionSummary
import com.bookmate.app.domain.usecase.GetRecap
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val session: AiChatSession,
    private val getBookSummary: GetBookSummary,
    private val getDescriptionSummary: GetDescriptionSummary,
    private val getChapterSummary: GetChapterSummary,
    private val getRecap: GetRecap,
    private val explainPart: ExplainPart,
    private val askAboutBook: AskAboutBook,
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
}