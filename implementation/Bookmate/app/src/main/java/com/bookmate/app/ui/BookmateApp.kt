package com.bookmate.app.ui

import androidx.annotation.StringRes
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
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookmate.app.R
import com.bookmate.app.ui.auth.LoginScreen
import com.bookmate.app.ui.auth.RegisterScreen
import com.bookmate.app.ui.onboarding.OnboardingScreen
import com.bookmate.app.ui.components.BookmateBottomNav
import com.bookmate.app.ui.navigation.Routes
import com.bookmate.app.ui.navigation.route
import com.bookmate.app.ui.navigation.tabForRoute
import com.bookmate.app.ui.screens.DevMenuScreen
import com.bookmate.app.ui.screens.PlaceholderScreen
import com.bookmate.app.ui.theme.BookmateGradient
import com.bookmate.app.ui.theme.Dimens

private fun NavGraphBuilder.placeholder(route: String, @StringRes titleRes: Int) {
    composable(route) { PlaceholderScreen(stringResource(titleRes)) }
}

private fun NavGraphBuilder.placeholderWithBook(route: String, @StringRes titleRes: Int) {
    composable(
        route = route,
        arguments = listOf(navArgument(Routes.BOOK_ID_ARG) { type = NavType.StringType })
    ) { entry ->
        PlaceholderScreen(
            title = stringResource(titleRes),
            subtitle = entry.arguments?.getString(Routes.BOOK_ID_ARG)
        )
    }
}

@Composable
fun BookmateApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = tabForRoute(backStackEntry?.destination?.route)

    val devItems = listOf(
        stringResource(R.string.screen_splash) to Routes.SPLASH,
        stringResource(R.string.screen_login) to Routes.LOGIN,
        stringResource(R.string.screen_register) to Routes.REGISTER,
        stringResource(R.string.screen_onboarding) to Routes.ONBOARDING,
        stringResource(R.string.screen_book_detail) to Routes.bookDetail("demo"),
        stringResource(R.string.screen_reader) to Routes.reader("demo"),
        stringResource(R.string.screen_scan) to Routes.SCAN,
        stringResource(R.string.screen_settings) to Routes.SETTINGS,
        stringResource(R.string.screen_subscription) to Routes.SUBSCRIPTION,
        stringResource(R.string.screen_checkout) to Routes.CHECKOUT,
        stringResource(R.string.screen_admin_add_book) to Routes.ADMIN_ADD_BOOK,
        stringResource(R.string.screen_admin_manage_books) to Routes.ADMIN_MANAGE_BOOKS,
        stringResource(R.string.screen_admin_subscribers) to Routes.ADMIN_SUBSCRIBERS,
        stringResource(R.string.screen_admin_plan_price) to Routes.ADMIN_PLAN_PRICE
    )

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
            placeholder(Routes.SPLASH, R.string.screen_splash)
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = { user ->
                        val target = if (user.onboardingDone) Routes.HOME else Routes.ONBOARDING
                        navController.navigate(target) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onCreateAccount = { navController.navigate(Routes.REGISTER) }
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegistered = {
                        navController.navigate(Routes.ONBOARDING) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onFinished = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            placeholder(Routes.HOME, R.string.nav_home)
            placeholder(Routes.SEARCH, R.string.nav_search)
            placeholder(Routes.AI, R.string.nav_ai)
            placeholder(Routes.MY_BOOKS, R.string.nav_my_books)
            composable(Routes.PROFILE) {
                DevMenuScreen(
                    title = stringResource(R.string.nav_profile),
                    items = devItems,
                    onOpen = { navController.navigate(it) }
                )
            }

            placeholderWithBook(Routes.BOOK_DETAIL, R.string.screen_book_detail)
            placeholderWithBook(Routes.READER, R.string.screen_reader)
            placeholder(Routes.SCAN, R.string.screen_scan)
            placeholder(Routes.SETTINGS, R.string.screen_settings)
            placeholder(Routes.SUBSCRIPTION, R.string.screen_subscription)
            placeholder(Routes.CHECKOUT, R.string.screen_checkout)

            placeholder(Routes.ADMIN_ADD_BOOK, R.string.screen_admin_add_book)
            placeholder(Routes.ADMIN_MANAGE_BOOKS, R.string.screen_admin_manage_books)
            placeholder(Routes.ADMIN_SUBSCRIBERS, R.string.screen_admin_subscribers)
            placeholder(Routes.ADMIN_PLAN_PRICE, R.string.screen_admin_plan_price)
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