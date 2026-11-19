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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookmate.app.R
import com.bookmate.app.domain.ai.ChatAction
import com.bookmate.app.ui.theme.DarkGreen
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

private val Cream = Color(0xFFFAF2DE)
private val SheetColor = Color(0xFFFBF9F4)
private val OutlineColor = Color(0xFFDAD8CE)

/**
 * SCR-08: the AI sheet over the Reader. It only chooses what to do; the answer streams in the
 * Book Chat. "Ask a question" opens the chat without a request (action = null).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderAiSheet(
    chapterNo: Int,
    page: Int,
    onAction: (ChatAction?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = SheetColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
            OptionRow(stringResource(R.string.reader_ai_summarize), null, false) {
                onAction(ChatAction.Summary)
            }
            OptionRow(
                stringResource(R.string.reader_ai_recap),
                stringResource(R.string.reader_ai_recap_hint),
                false
            ) { onAction(ChatAction.Recap) }
            OptionRow(stringResource(R.string.reader_ai_explain), null, false) {
                onAction(ChatAction.Explain)
            }
            OptionRow(stringResource(R.string.reader_ai_ask), null, true) { onAction(null) }
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