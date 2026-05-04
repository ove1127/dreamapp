package com.dreamweddingstories.tv.navigation

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dreamweddingstories.tv.screens.EventSelectionScreen
import com.dreamweddingstories.tv.screens.HomeScreen
import com.dreamweddingstories.tv.screens.LoginScreen
import com.dreamweddingstories.tv.screens.SplashScreen
import com.dreamweddingstories.tv.screens.VideoDetailScreen
import com.dreamweddingstories.tv.screens.VideoPlayerScreen
import com.dreamweddingstories.tv.ui.theme.DreamAnimation
import com.dreamweddingstories.tv.viewmodel.AuthViewModel
import com.dreamweddingstories.tv.viewmodel.HomeViewModel
import com.dreamweddingstories.tv.viewmodel.PlayerViewModel

private sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Login : AppRoute("login")
    data object EventSelect : AppRoute("eventSelect")
    data object Home : AppRoute("home")
    data object VideoDetail : AppRoute("videoDetail/{videoId}") {
        fun create(videoId: String) = "videoDetail/$videoId"
    }
    data object VideoPlayer : AppRoute("videoPlayer/{videoId}/{vimeoVideoId}/{title}") {
        fun create(videoId: String, vimeoVideoId: String, title: String): String {
            return "videoPlayer/${Uri.encode(videoId)}/${Uri.encode(vimeoVideoId)}/${Uri.encode(title)}"
        }
    }
}

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authState = authViewModel.authState.collectAsState().value
    val currentUser = authViewModel.currentUser.collectAsState().value
    val videosState = homeViewModel.videosState.collectAsState().value
    val eventsState = homeViewModel.eventsState.collectAsState().value
    val selectedVideoState = homeViewModel.selectedVideoState.collectAsState().value

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
        enterTransition = { DreamAnimation.screenEnter() },
        exitTransition = { DreamAnimation.screenExit() },
        popEnterTransition = { DreamAnimation.screenEnter() },
        popExitTransition = { DreamAnimation.screenExit() }
    ) {
        // ── Splash ──
        composable(
            route = AppRoute.Splash.route,
            enterTransition = { DreamAnimation.screenEnter() },
            exitTransition = { DreamAnimation.screenExit() }
        ) {
            SplashScreen(
                onFinished = {
                    val target = if (authViewModel.hasActiveSession()) {
                        AppRoute.EventSelect.route
                    } else {
                        AppRoute.Login.route
                    }
                    navController.navigate(target) {
                        popUpTo(AppRoute.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ──
        composable(
            route = AppRoute.Login.route,
            enterTransition = { DreamAnimation.screenEnter() },
            exitTransition = { DreamAnimation.screenExit() }
        ) {
            LoginScreen(
                authState = authState,
                onCodeSignIn = authViewModel::signInWithCode,
                onDemoSignIn = authViewModel::signInAsDemoUser,
                onDismissError = authViewModel::clearAuthError,
                onLoginSuccess = {
                    navController.navigate(AppRoute.EventSelect.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Event Selection (NEW — Netflix-style function picker) ──
        composable(route = AppRoute.EventSelect.route) {
            val activity = LocalContext.current as? Activity

            BackHandler {
                activity?.finish()
            }

            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.EventSelect.route) { inclusive = true }
                    }
                }
            } else {
                // Load events from user object
                LaunchedEffect(currentUser.uid) {
                    homeViewModel.loadEvents(currentUser)
                }

                EventSelectionScreen(
                    user = currentUser,
                    eventsState = eventsState,
                    onEventSelected = { event ->
                        homeViewModel.loadVideosForEvent(event)
                        navController.navigate(AppRoute.Home.route)
                    },
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.EventSelect.route) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ── Home (filtered to selected event's videos) ──
        composable(route = AppRoute.Home.route) {
            val activity = LocalContext.current as? Activity

            // Back from Home → go back to event selection
            BackHandler {
                navController.navigate(AppRoute.EventSelect.route) {
                    popUpTo(AppRoute.Home.route) { inclusive = true }
                }
            }

            if (currentUser == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(AppRoute.Login.route) {
                        popUpTo(AppRoute.Home.route) { inclusive = true }
                    }
                }
            } else {
                HomeScreen(
                    user = currentUser,
                    videosState = videosState,
                    onVideoSelected = { videoId ->
                        navController.navigate(AppRoute.VideoDetail.create(videoId))
                    },
                    onLogout = {
                        authViewModel.signOut()
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(AppRoute.Home.route) { inclusive = true }
                        }
                    },
                    onRetry = {
                        currentUser.let { u ->
                            homeViewModel.loadAssignedVideos(u)
                        }
                    }
                )
            }
        }

        // ── Video Detail ──
        composable(
            route = AppRoute.VideoDetail.route,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId").orEmpty()
            LaunchedEffect(videoId) {
                homeViewModel.loadVideoById(videoId)
            }

            VideoDetailScreen(
                state = selectedVideoState,
                onBack = { navController.popBackStack() },
                onPlay = { video ->
                    navController.navigate(
                        AppRoute.VideoPlayer.create(video.id, video.vimeoVideoId, video.coupleNames)
                    )
                },
                onRetry = { homeViewModel.loadVideoById(videoId) }
            )
        }

        // ── Video Player ──
        composable(
            route = AppRoute.VideoPlayer.route,
            arguments = listOf(
                navArgument("videoId") { type = NavType.StringType },
                navArgument("vimeoVideoId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            ),
            enterTransition = { DreamAnimation.playerEnter() },
            exitTransition = { DreamAnimation.playerExit() }
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getString("videoId").orEmpty()
            val vimeoVideoId = backStackEntry.arguments?.getString("vimeoVideoId").orEmpty()
            val title = backStackEntry.arguments?.getString("title").orEmpty()

            VideoPlayerScreen(
                videoId = videoId,
                vimeoVideoId = vimeoVideoId,
                title = title,
                viewModel = playerViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
