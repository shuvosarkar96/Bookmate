package com.bookmate.app.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayDate: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

/** Formats epoch milliseconds like "18 Dec 2026" in the device time zone. */
fun formatDate(millis: Long): String =
    displayDate.format(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()))