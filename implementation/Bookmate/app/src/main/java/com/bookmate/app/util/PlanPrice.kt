package com.bookmate.app.util

import java.util.Locale

private val PRICE_REGEX = Regex("^\\d{1,4}(\\.\\d{1,2})?$")

/** A price above 0 with at most 4 digits before and 2 after the dot. Null when not valid. */
fun parsePlanPrice(text: String): Double? {
    val trimmed = text.trim()
    if (!PRICE_REGEX.matches(trimmed)) return null
    return trimmed.toDouble().takeIf { it > 0.0 }
}

/** The text shown in the app, for example "$4.99 / month (sample)". */
fun planPriceLabel(price: Double): String =
    String.format(Locale.US, "\$%.2f / month (sample)", price)