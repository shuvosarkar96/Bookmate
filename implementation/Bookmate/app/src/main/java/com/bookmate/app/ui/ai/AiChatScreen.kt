package com.bookmate.app.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bookmate.app.R
import com.bookmate.app.domain.ai.ChatAction
import com.bookmate.app.domain.ai.ChatContext
import com.bookmate.app.domain.ai.ChatMessage
import com.bookmate.app.domain.ai.ChatRole
import com.bookmate.app.domain.ai.PromptBuilder
import com.bookmate.app.domain.model.Book
import com.bookmate.app.domain.model.ReadingProgress
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.theme.CardSurface
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.SecondaryContainerColor
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

private val Cream = Color(0xFFFAF2DE)

/** SCR-09: the AI tab with Book Chat and Recommend. */
@Composable
fun AiChatScreen(
    onFindBook: () -> Unit,
    onBookClick: (String) -> Unit = {},
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val chat by viewModel.context.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val answer by viewModel.answer.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val picker by viewModel.picker.collectAsStateWithLifecycle()
    LaunchedEffect(chat == null) { if (chat == null) viewModel.loadPicker() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = Dimens.SideMargin)
            .padding(bottom = 112.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.ai_chat_title),
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Green,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        ModeToggle(selected = tab, onSelect = { tab = it })
        Spacer(modifier = Modifier.height(12.dp))
        if (tab == 0) {
            BookChat(
                chat = chat,
                messages = messages,
                answer = answer,
                onFindBook = onFindBook,
                onSend = viewModel::send,
                onRetry = viewModel::retry,
                picker = picker,
                onPick = viewModel::pick,
                modifier = Modifier.weight(1f)
            )
        } else {
            RecommendTab(onBookClick = onBookClick, modifier = Modifier.weight(1f))

        }
    }
}

@Composable
private fun ModeToggle(selected: Int, onSelect: (Int) -> Unit) {
    val labels = listOf(
        stringResource(R.string.ai_chat_tab_book),
        stringResource(R.string.ai_chat_tab_recommend)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(SecondaryContainerColor)
            .padding(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) Green else Color.Transparent)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Cream else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun BookChat(
    chat: ChatContext?,
    messages: List<ChatMessage>,
    answer: AiAnswerState,
    onFindBook: () -> Unit,
    onSend: (ChatAction) -> Unit,
    onRetry: () -> Unit,
    picker: PickerState,
    onPick: (Book, ReadingProgress?) -> Unit,
    modifier: Modifier = Modifier
) {
    if (chat == null) {
        PickBook(picker = picker, onPick = onPick, onFindBook = onFindBook, modifier = modifier)
        return
    }

    val listState = rememberLazyListState()
    val itemCount = messages.size + if (answer is AiAnswerState.Idle) 0 else 1
    LaunchedEffect(itemCount, answer) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }
    var input by rememberSaveable { mutableStateOf("") }
    val busy = answer.isBusy

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(CardSurface)
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (chat.isBookLevel) chat.book.title
                else stringResource(R.string.ai_chat_context, chat.book.title, chat.chapter),
                fontSize = 13.sp,
                color = TextPrimary
            )
        }
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { message -> MessageBubble(message) }
            if (answer !is AiAnswerState.Idle) {
                item { AiAnswerCard(state = answer, onRetry = onRetry) }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickChip(stringResource(R.string.reader_ai_summarize), !busy) { onSend(ChatAction.Summary) }
            QuickChip(stringResource(R.string.reader_ai_recap), !busy && !chat.isBookLevel) { onSend(ChatAction.Recap) }
            QuickChip(stringResource(R.string.reader_ai_explain), !busy && !chat.isBookLevel) { onSend(ChatAction.Explain) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val sendLabel = stringResource(R.string.ai_chat_send)
        val canSend = input.isNotBlank() && !busy
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { if (it.length <= PromptBuilder.MAX_QUESTION_LENGTH) input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(stringResource(R.string.ai_chat_input_hint)) },
                maxLines = 3,
                shape = RoundedCornerShape(26.dp)
            )
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (canSend) Green else Green.copy(alpha = 0.4f))
                    .semantics { contentDescription = sendLabel }
                    .clickable(enabled = canSend) {
                        onSend(ChatAction.Question(input.trim()))
                        input = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "↑", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Cream)
            }
        }
        Text(
            text = stringResource(
                R.string.reader_ai_counter,
                input.length,
                PromptBuilder.MAX_QUESTION_LENGTH
            ),
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
        )
    }
}

@Composable
private fun QuickChip(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (enabled) SecondaryContainerColor else SecondaryContainerColor.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (enabled) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == ChatRole.USER
    val text = when (val action = message.action) {
        ChatAction.Summary -> stringResource(R.string.reader_ai_summarize)
        ChatAction.Recap -> stringResource(R.string.reader_ai_recap)
        ChatAction.Explain -> stringResource(R.string.reader_ai_explain)
        is ChatAction.Question -> action.text
        null -> message.text
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isUser) Green else CardSurface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isUser) {
                Text(text = text, fontSize = 15.sp, color = Cream)
            } else {
                SelectionContainer {
                    Text(text = text, fontSize = 15.sp, color = TextPrimary)
                }
                Text(
                    text = stringResource(R.string.ai_answer_notice),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}