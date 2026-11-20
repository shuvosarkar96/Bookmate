package com.bookmate.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlanPriceTest {

    @Test
    fun whole_and_decimal_prices_are_accepted() {
        assertEquals(4.99, parsePlanPrice("4.99")!!, 0.0001)
        assertEquals(5.0, parsePlanPrice(" 5 ")!!, 0.0001)
    }

    @Test
    fun zero_text_and_too_many_decimals_are_rejected() {
        assertNull(parsePlanPrice("0"))
        assertNull(parsePlanPrice("abc"))
        assertNull(parsePlanPrice(""))
        assertNull(parsePlanPrice("4.999"))
        assertNull(parsePlanPrice("-3"))
    }

    @Test
    fun the_label_always_has_two_decimals() {
        assertEquals("\$4.99 / month (sample)", planPriceLabel(4.99))
        assertEquals("\$5.00 / month (sample)", planPriceLabel(5.0))
    }
}