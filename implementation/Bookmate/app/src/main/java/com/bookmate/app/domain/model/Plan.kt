package com.bookmate.app.domain.model

/** The one Bookmate Premium plan (document config/plan). */
data class Plan(
    val name: String,
    val price: Double,
    val priceLabel: String,
    val periodDays: Int
)