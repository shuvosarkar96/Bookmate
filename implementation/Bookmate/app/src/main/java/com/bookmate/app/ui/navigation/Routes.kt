package com.bookmate.app.ui.navigation

import com.bookmate.app.ui.components.BottomNavTab

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val ONBOARDING = "onboarding"

    const val HOME = "home"
    const val SEARCH = "search"
    const val AI = "ai"
    const val MY_BOOKS = "my_books"
    const val PROFILE = "profile"

    const val BOOK_ID_ARG = "bookId"
    const val BOOK_DETAIL = "book/{bookId}"
    const val READER = "reader/{bookId}"
    const val SCAN = "scan"
    const val SETTINGS = "settings"
    const val SUBSCRIPTION = "subscription"
    const val CHECKOUT = "checkout"

    const val ADMIN_ADD_BOOK = "admin/add_book"
    const val ADMIN_MANAGE_BOOKS = "admin/manage_books"
    const val ADMIN_SUBSCRIBERS = "admin/subscribers"
    const val ADMIN_PLAN_PRICE = "admin/plan_price"

    fun bookDetail(bookId: String) = "book/$bookId"
    fun reader(bookId: String) = "reader/$bookId"
}

fun BottomNavTab.route(): String = when (this) {
    BottomNavTab.Home -> Routes.HOME
    BottomNavTab.Search -> Routes.SEARCH
    BottomNavTab.Ai -> Routes.AI
    BottomNavTab.MyBooks -> Routes.MY_BOOKS
    BottomNavTab.Profile -> Routes.PROFILE
}

fun tabForRoute(route: String?): BottomNavTab? =
    BottomNavTab.entries.firstOrNull { it.route() == route }