package com.bookmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bookmate.app.domain.repository.AuthRepository
import com.bookmate.app.ui.BookmateApp
import com.bookmate.app.ui.theme.BookmateTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Temporary: proves the Hilt graph works. Removed in Step 1.4e.
    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BookmateTheme {
                BookmateApp()
            }
        }
    }
}