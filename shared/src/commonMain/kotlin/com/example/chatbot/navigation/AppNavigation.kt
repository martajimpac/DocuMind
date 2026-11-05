package com.example.chatbot.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.chatbot.ui.screens.detail.NotebookDetailScreen
import com.example.chatbot.ui.screens.forgotpassword.ForgotPasswordScreen
import com.example.chatbot.ui.screens.login.LoginScreen
import com.example.chatbot.ui.screens.notebooks.NotebooksScreen
import com.example.chatbot.ui.screens.register.RegisterScreen
import com.example.chatbot.ui.screens.resetpassword.ResetPasswordScreen
import com.example.chatbot.ui.utils.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

/**
 * App navigation graph.
 *
 * @param incomingLink the last link the platform opened the app with (Android: the intent data),
 * or null. Password reset links open «New password».
 */
@Composable
fun AppNavigation(
    incomingLink: String?,
    sessionViewModel: SessionViewModel = koinViewModel(),
) {
    val startDestination by sessionViewModel.startDestination.collectAsStateWithLifecycle()
    // Until the saved session has been read, show only the background so the login never flashes.
    val start = startDestination ?: run {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }
    val navController = rememberNavController()

    ObserveAsEvents(sessionViewModel.events) { event ->
        when (event) {
            SessionEvent.SignedOut -> navController.clearAndGoToLogin()
        }
    }

    NavHost(
        navController = navController,
        startDestination = when (start) {
            StartDestination.Login -> LoginRoute
            StartDestination.Notebooks -> NotebooksRoute
        },
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onSignedIn = {
                    navController.navigate(NotebooksRoute) {
                        popUpTo<LoginRoute> { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(RegisterRoute) { launchSingleTop = true }
                },
                onForgotPasswordClick = { email ->
                    navController.navigate(ForgotPasswordRoute(email)) { launchSingleTop = true }
                },
            )
        }

        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(onBackToLogin = { navController.backToLogin() })
        }

        composable<ResetPasswordRoute> {
            ResetPasswordScreen(
                onPasswordUpdated = {
                    navController.navigate(NotebooksRoute) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCancelled = { navController.clearAndGoToLogin() },
                onRequestNewLink = {
                    navController.navigate(ForgotPasswordRoute()) {
                        popUpTo<ResetPasswordRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<RegisterRoute> {
            RegisterScreen(onBackToLogin = { navController.popBackStack<LoginRoute>(inclusive = false) })
        }

        composable<NotebooksRoute> {
            NotebooksScreen(
                onOpenNotebook = { id, title ->
                    navController.navigate(NotebookDetailRoute(notebookId = id, title = title)) { launchSingleTop = true }
                },
                onSignOut = sessionViewModel::signOut,
            )
        }

        composable<NotebookDetailRoute> {
            // The ViewModel reads notebookId and title from the route through SavedStateHandle.
            NotebookDetailScreen(onBackClick = { navController.popBackStack() })
        }
    }

    // After NavHost so the graph exists when the launch link is opened.
    HandleResetLinks(incomingLink, navController)
}

private fun NavController.clearAndGoToLogin() {
    navigate(LoginRoute) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavController.backToLogin() {
    if (!popBackStack<LoginRoute>(inclusive = false)) clearAndGoToLogin()
}

/**
 * Opens «New password» for documind://reset-password links. Each link is handled once, also across
 * rotation and process death, so a used code is not opened again when the activity is recreated
 * with the same launch link.
 */
@Composable
private fun HandleResetLinks(incomingLink: String?, navController: NavController) {
    var lastHandledLink by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(incomingLink) {
        val link = incomingLink ?: return@LaunchedEffect
        if (link == lastHandledLink) return@LaunchedEffect
        val resetLink = ResetLink.parse(link) ?: return@LaunchedEffect
        lastHandledLink = link
        navController.navigate(ResetPasswordRoute(resetLink.code, resetLink.errorCode)) {
            launchSingleTop = true
        }
    }
}
