package com.bookmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bookmate.app.ui.BookmateApp
import com.bookmate.app.ui.theme.BookmateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookmateTheme {
                BookmateApp()
            }
        }
    }
}