package com.bookmate.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.ui.theme.Green
import com.bookmate.app.ui.theme.InputFill
import com.bookmate.app.ui.theme.OutlineColor
import com.bookmate.app.ui.theme.TextPrimary
import com.bookmate.app.ui.theme.TextSecondary

@Composable
fun BookmateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    val shape = MaterialTheme.shapes.small
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.FieldHeight),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
        cursorBrush = SolidColor(Green),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(InputFill, shape)
                    .border(1.dp, OutlineColor, shape)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
                innerTextField()
            }
        }
    )
}