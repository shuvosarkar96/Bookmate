package com.bookmate.app.util

import com.bookmate.app.domain.repository.SettingsRepository

/** Position of [sizeSp] in the list of text sizes. An unknown size gives the position of the default size. */
fun textSizeIndex(sizeSp: Int): Int {
    val index = SettingsRepository.TEXT_SIZES.indexOf(sizeSp)
    return if (index >= 0) index else SettingsRepository.TEXT_SIZES.indexOf(SettingsRepository.DEFAULT_TEXT_SIZE)
}

/** The text size at a slider position. Positions outside the list are clamped. */
fun textSizeAt(position: Int): Int =
    SettingsRepository.TEXT_SIZES[position.coerceIn(0, SettingsRepository.TEXT_SIZES.lastIndex)]