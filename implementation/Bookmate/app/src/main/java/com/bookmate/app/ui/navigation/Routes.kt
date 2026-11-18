package com.bookmate.app.ui.navigation

import com.bookmate.app.ui.components.BottomNavTab

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val AI = "ai"
    const val MY_BOOKS = "my_books"
    const val PROFILE = "profile"
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