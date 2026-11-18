package com.bookmate.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bookmate.app.R
import com.bookmate.app.ui.components.BookmateBottomNav
import com.bookmate.app.ui.navigation.Routes
import com.bookmate.app.ui.navigation.route
import com.bookmate.app.ui.navigation.tabForRoute
import com.bookmate.app.ui.screens.PlaceholderScreen
import com.bookmate.app.ui.theme.BookmateGradient
import com.bookmate.app.ui.theme.Dimens

@Composable
fun BookmateApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = tabForRoute(backStackEntry?.destination?.route)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BookmateGradient)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.HOME) { PlaceholderScreen(stringResource(R.string.nav_home)) }
            composable(Routes.SEARCH) { PlaceholderScreen(stringResource(R.string.nav_search)) }
            composable(Routes.AI) { PlaceholderScreen(stringResource(R.string.nav_ai)) }
            composable(Routes.MY_BOOKS) { PlaceholderScreen(stringResource(R.string.nav_my_books)) }
            composable(Routes.PROFILE) { PlaceholderScreen(stringResource(R.string.nav_profile)) }
        }

        if (currentTab != null) {
            BookmateBottomNav(
                selected = currentTab,
                onSelect = { tab ->
                    navController.navigate(tab.route()) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = Dimens.NavSideMargin)
                    .padding(bottom = 24.dp)
            )
        }
    }
}