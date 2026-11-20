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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.bookmate.app.R
import com.bookmate.app.ui.auth.LoginScreen
import com.bookmate.app.ui.home.HomeScreen
import com.bookmate.app.ui.subscription.CheckoutScreen
import com.bookmate.app.ui.reader.ReaderScreen
import com.bookmate.app.ui.ai.AiChatScreen
import com.bookmate.app.ui.subscription.SubscriptionScreen
import com.bookmate.app.ui.detail.BookDetailScreen
import com.bookmate.app.ui.search.SearchScreen
import com.bookmate.app.ui.profile.ProfileScreen
import com.bookmate.app.ui.auth.RegisterScreen
import com.bookmate.app.ui.onboarding.OnboardingScreen
import com.bookmate.app.ui.splash.SplashScreen
import com.bookmate.app.ui.components.BookmateBottomNav
import com.bookmate.app.ui.navigation.Routes
import com.bookmate.app.ui.navigation.route
import com.bookmate.app.ui.navigation.tabForRoute
import com.bookmate.app.ui.screens.PlaceholderScreen
import com.bookmate.app.ui.theme.BookmateGradient
import com.bookmate.app.ui.theme.Dimens
import com.bookmate.app.util.restartApp

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

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BookmateGradient)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onResolved = { target ->
                        navController.navigate(target) {
                            popUpTo(Routes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }
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
                    onBack = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onContinueClick = { bookId -> navController.navigate(Routes.reader(bookId)) },
                    onSearchClick = {
                        navController.navigate(Routes.SEARCH) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onScanClick = { navController.navigate(Routes.SCAN) },
                    onAskClick = {
                        navController.navigate(Routes.AI) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onBookClick = { bookId -> navController.navigate(Routes.bookDetail(bookId)) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onBookClick = { bookId -> navController.navigate(Routes.bookDetail(bookId)) }
                )
            }
            composable(Routes.AI) {
                AiChatScreen(
                    onBookClick = { bookId -> navController.navigate(Routes.bookDetail(bookId)) },
                    
                    onFindBook = {
                        navController.navigate(Routes.SEARCH) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            placeholder(Routes.MY_BOOKS, R.string.nav_my_books)
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onSubscribe = { navController.navigate(Routes.SUBSCRIPTION) },
                    onContinue = { bookId -> navController.navigate(Routes.reader(bookId)) },
                    onOpenMyBooks = {
                        navController.navigate(Routes.MY_BOOKS) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenAdmin = { route -> navController.navigate(route) },
                    onLoggedOut = { restartApp(context) }
                )
            }

            composable(
                route = Routes.BOOK_DETAIL,
                arguments = listOf(navArgument(Routes.BOOK_ID_ARG) { type = NavType.StringType })
            ) {
                BookDetailScreen(
                    onBack = { navController.popBackStack() },
                    onRead = { bookId -> navController.navigate(Routes.reader(bookId)) },
                    onSubscribe = { navController.navigate(Routes.SUBSCRIPTION) },
                    onOpenChat = { navController.navigate(Routes.AI) { launchSingleTop = true } }
                )
            }
            composable(
                route = Routes.READER,
                arguments = listOf(navArgument(Routes.BOOK_ID_ARG) { type = NavType.StringType })
            ) {
                ReaderScreen(
                    onBack = { navController.popBackStack() },
                    onSubscribe = { navController.navigate(Routes.SUBSCRIPTION) },
                    onOpenChat = {
                        navController.navigate(Routes.AI) {
                            popUpTo(Routes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            placeholder(Routes.SCAN, R.string.screen_scan)
            placeholder(Routes.SETTINGS, R.string.screen_settings)
            placeholder(Routes.NOTIFICATIONS, R.string.screen_notifications)
            composable(Routes.SUBSCRIPTION) {
                SubscriptionScreen(
                    onBack = { navController.popBackStack() },
                    onSubscribe = { navController.navigate(Routes.CHECKOUT) }
                )
            }
            composable(Routes.CHECKOUT) {
                CheckoutScreen(
                    onDone = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

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
                        popUpTo(Routes.HOME) {
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