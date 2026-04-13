package com.dreamweddingstories.tv.navigation

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.dreamweddingstories.tv.screens.HomeScreen
import com.dreamweddingstories.tv.screens.LoginScreen
import com.dreamweddingstories.tv.screens.SplashScreen
import com.dreamweddingstories.tv.screens.VideoDetailScreen
import com.dreamweddingstories.tv.screens.VideoPlayerScreen
import com.dreamweddingstories.tv.viewmodel.AuthViewModel
import com.dreamweddingstories.tv.viewmodel.HomeViewModel
import com.dreamweddingstories.tv.viewmodel.PlayerViewModel

private sealed class AppRoute(val route: String) {
    data object Splash : AppRoute("splash")
    data object Login : AppRoute("login")
    data object Home : AppRoute("home")
    data object VideoDetail : AppRoute("videoDetail/{videoId}") {
        fun create(videoId: String) = "videoDetail/$videoId"
    }
    data object VideoPlayer : AppRoute("videoPlayer/{videoId}/{vimeoVideoId}/{title}") {
        fun create(videoId: String, vimeoVideoId: String, title: String): String {
            return "videoPlayer/$videoId/$vimeoVideoId/${Uri.encode(title)}"
        }
    }
}

// ── Transition helpers ──
private const val TRANSITION_MS = 350

private fun enterSlideIn(): EnterTransition =
    fadeIn(tween(TRANSITION_MS)) + slideInHorizontally(
        initialOffsetX = { it / 4 },
        animationSpec = tween(TRANSITION_MS)
    )

private fun exitSlideOut(): ExitTransition =
    fadeOut(tween(TRANSITION_MS)) + slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = tween(TRANSITION_MS)
    )

private fun popEnterSlideIn(): EnterTransition =
    fadeIn(tween(TRANSITION_MS)) + slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(TRANSITION_MS)
    )

private fun popExitSlideOut(): ExitTransition =
    fadeOut(tween(TRANSITION_MS)) + slideOutHorizontally(
        targetOffsetX = { it / 4 },
        animationSpec = tween(TRANSITION_MS)
    )

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
    val selectedVideoState = homeViewModel.selectedVideoState.collectAsState().value

    // Load videos when user is available
    LaunchedEffect(currentUser?.uid) {
        currentUser?.let { homeViewModel.loadAssignedVideos(it) }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Splash.route,
        enterTransition = { enterSlideIn() },
        exitTransition = { exitSlideOut() },
        popEnterTransition = { popEnterSlideIn() },
        popExitTransition = { popExitSlideOut() }
    ) {
        // ── Splash ──
        composable(
            route = AppRoute.Splash.route,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(400)) }
        ) {
            SplashScreen(
                onFinished = {
                    val target = if (authViewModel.hasActiveSession()) {
                        AppRoute.Home.route
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
            enterTransition = { fadeIn(tween(400)) },
            exitTransition = { fadeOut(tween(300)) }
        ) {
            LoginScreen(
                authState = authState,
                onEmailPasswordSignIn = authViewModel::signIn,
                onDemoSignIn = authViewModel::signInAsDemoUser,
                onDismissError = authViewModel::clearAuthError,
                onLoginSuccess = {
                    navController.navigate(AppRoute.Home.route) {
                        popUpTo(AppRoute.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──
        composable(route = AppRoute.Home.route) {
            val activity = LocalContext.current as? Activity

            // Back from Home = exit app
            BackHandler {
                activity?.finish()
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
                    onRetry = { homeViewModel.loadAssignedVideos(currentUser) }
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
            // Immediate transition for player
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) }
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
