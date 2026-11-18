package com.bookmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bookmate.app.ui.components.BookmateCard
import com.bookmate.app.ui.components.BookmateChip
import com.bookmate.app.ui.components.BookmateTextField
import com.bookmate.app.ui.components.PrimaryButton
import com.bookmate.app.ui.theme.BookmateGradient
import com.bookmate.app.ui.theme.BookmateTheme
import com.bookmate.app.ui.theme.Dimens

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookmateTheme {
                Greeting()
            }
        }
    }
}

@Composable
fun Greeting() {
    var text by remember { mutableStateOf("") }
    var firstChip by remember { mutableStateOf(true) }
    val name = stringResource(R.string.app_name)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BookmateGradient)
            .padding(horizontal = Dimens.SideMargin),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BookmateChip(text = name, selected = firstChip, onClick = { firstChip = true })
            BookmateChip(text = name, selected = !firstChip, onClick = { firstChip = false })
        }
        BookmateCard {
            Text(text = name, style = MaterialTheme.typography.titleLarge)
            Text(text = name, style = MaterialTheme.typography.bodyMedium)
        }
        BookmateTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = name
        )
        PrimaryButton(text = name, onClick = { })
    }
}