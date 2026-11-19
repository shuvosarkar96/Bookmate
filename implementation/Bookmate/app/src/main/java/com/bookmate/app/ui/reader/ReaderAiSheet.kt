package com.bookmate.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookmate.app.R
import com.bookmate.app.domain.ai.PromptBuilder
import com.bookmate.app.ui.ai.AiAnswerCard
import com.bookmate.app.ui.ai.AiAnswerState
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

private val Cream = Color(0xFFFAF2DE)
private val SheetColor = Color(0xFFFBF9F4)
private val OutlineColor = Color(0xFFDAD8CE)

/**
 * SCR-08: the AI sheet over the Reader. Four options. The answer shows inside the sheet for now;
 * Step 4.6 moves it to the Book Chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderAiSheet(
    chapterNo: Int,
    page: Int,
    aiState: AiAnswerState,
    onSummarize: () -> Unit,
    onRecap: () -> Unit,
    onExplain: () -> Unit,
    onAsk: (String) -> Unit,
    onRetry: () -> Unit,
    onBackToOptions: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var asking by remember { mutableStateOf(false) }
    var question by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = SheetColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.reader_ai_title),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = stringResource(R.string.reader_ai_position, chapterNo, page),
                fontSize = 13.sp,
                color = TextSecondary
            )
            when {
                aiState !is AiAnswerState.Idle -> {
                    AiAnswerCard(state = aiState, onRetry = onRetry)
                    TextButton(onClick = {
                        asking = false
                        question = ""
                        onBackToOptions()
                    }) {
                        Text(stringResource(R.string.reader_ai_back), color = DarkGreen)
                    }
                }

                asking -> {
                    OutlinedTextField(
                        value = question,
                        onValueChange = {
                            if (it.length <= PromptBuilder.MAX_QUESTION_LENGTH) question = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.reader_ai_question_hint)) },
                        minLines = 2,
                        maxLines = 5
                    )
                    Text(
                        text = stringResource(
                            R.string.reader_ai_counter,
                            question.length,
                            PromptBuilder.MAX_QUESTION_LENGTH
                        ),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    PrimaryButton(
                        text = stringResource(R.string.reader_ai_send),
                        onClick = { onAsk(question.trim()) },
                        enabled = question.isNotBlank()
                    )
                    TextButton(onClick = { asking = false }) {
                        Text(stringResource(R.string.reader_ai_back), color = DarkGreen)
                    }
                }

                else -> {
                    OptionRow(stringResource(R.string.reader_ai_summarize), null, false, onSummarize)
                    OptionRow(
                        stringResource(R.string.reader_ai_recap),
                        stringResource(R.string.reader_ai_recap_hint),
                        false,
                        onRecap
                    )
                    OptionRow(stringResource(R.string.reader_ai_explain), null, false, onExplain)
                    OptionRow(stringResource(R.string.reader_ai_ask), null, true) { asking = true }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.ai_answer_notice),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun OptionRow(text: String, hint: String?, filled: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(if (filled) Green else Color.White)
            .then(if (filled) Modifier else Modifier.border(1.dp, OutlineColor, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (filled) Cream else Green)
        )
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = if (filled) FontWeight.Bold else FontWeight.Normal,
            color = if (filled) Cream else TextPrimary
        )
        if (hint != null) {
            Text(text = hint, fontSize = 13.sp, color = TextSecondary)
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(text = "›", fontSize = 22.sp, color = if (filled) Cream else TextSecondary)
    }
}