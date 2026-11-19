package com.bookmate.app.ui.onboarding

import androidx.annotation.StringRes
import com.bookmate.app.R

enum class Genre(val id: String, @StringRes val labelRes: Int) {
    CLASSIC("Classic", R.string.genre_classic),
    MYSTERY("Mystery", R.string.genre_mystery),
    SCIENCE("Science", R.string.genre_science),
    FANTASY("Fantasy", R.string.genre_fantasy),
    ROMANCE("Romance", R.string.genre_romance),
    HISTORY("History", R.string.genre_history),
    ADVENTURE("Adventure", R.string.genre_adventure),
    POETRY("Poetry", R.string.genre_poetry),
    BIOGRAPHY("Biography", R.string.genre_biography),
    HORROR("Horror", R.string.genre_horror),
    TECHNOLOGY("Technology", R.string.genre_technology),
    SELF_HELP("Self-help", R.string.genre_self_help)
}