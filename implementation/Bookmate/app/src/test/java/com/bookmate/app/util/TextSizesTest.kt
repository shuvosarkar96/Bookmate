package com.bookmate.app.util

import com.bookmate.app.domain.repository.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Test

class TextSizesTest {
    @Test
    fun a_known_size_gives_its_position() {
        assertEquals(0, textSizeIndex(15))
        assertEquals(3, textSizeIndex(24))
    }

    @Test
    fun an_unknown_size_gives_the_position_of_the_default() {
        val defaultIndex = SettingsRepository.TEXT_SIZES.indexOf(SettingsRepository.DEFAULT_TEXT_SIZE)
        assertEquals(defaultIndex, textSizeIndex(99))
    }

    @Test
    fun a_position_gives_its_size() {
        assertEquals(20, textSizeAt(2))
    }

    @Test
    fun positions_outside_the_list_are_clamped() {
        assertEquals(15, textSizeAt(-3))
        assertEquals(24, textSizeAt(10))
    }
}