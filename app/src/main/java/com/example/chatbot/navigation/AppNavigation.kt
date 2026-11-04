package com.example.chatbot.navigation

import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.util.Consumer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatbot.R
import com.example.chatbot.ui.screens.detail.NotebookDetailScreen
import com.example.chatbot.ui.screens.forgotpassword.ForgotPasswordScreen
import com.example.chatbot.ui.screens.login.LoginScreen
import com.example.chatbot.ui.screens.notebooks.NotebooksScreen
import com.example.chatbot.ui.screens.register.RegisterScreen
import com.example.chatbot.ui.screens.resetpassword.ResetPasswordScreen
import com.example.chatbot.ui.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel


sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password?email={email}") {
        fun createRoute(email: String) = "forgot_password?email=${Uri.encode(email)}"
    }
    data object ResetPassword : Screen("reset_password?code={code}&error_code={error_code}") {
        fun createRoute(code: String?, errorCode: String?) = buildString {
            append("reset_password")
            val params = listOfNotNull(
                code?.let { "code=${Uri.encode(it)}" },
                errorCode?.let { "error_code=${Uri.encode(it)}" },
            )
            if (params.isNotEmpty()) append(params.joinToString("&", prefix = "?"))
        }
    }
    data object Notebooks : Screen("notebooks")
    data object NotebookDetail : Screen("notebook_detail/{title}") {
        fun createRoute(title: String) = "notebook_detail/${Uri.encode(title)}"
    }
}

@Composable
fun AppNavigation(sessionViewModel: SessionViewModel = koinViewModel()) {
    val startDestination by sessionViewModel.startDestination.collectAsStateWithLifecycle()
    // Until the saved session has been read, show only the background so the login never flashes.
    val start = startDestination ?: run {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }
    val navController = rememberNavController()

    ObserveAsEvents(sessionViewModel.events) { event ->
        when (event) {
            SessionEvent.SignedOut -> navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = when (start) {
            StartDestination.Login -> Screen.Login.route
            StartDestination.Notebooks -> Screen.Notebooks.route
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onSignedIn = {
                    navController.navigate(Screen.Notebooks.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route) { launchSingleTop = true }
                },
                onForgotPasswordClick = { email ->
                    navController.navigate(Screen.ForgotPassword.createRoute(email)) { launchSingleTop = true }
                }
            )
        }

        composable(
            route = Screen.ForgotPassword.route,
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) {
            ForgotPasswordScreen(onBackToLogin = { navController.backToLogin() })
        }

        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(
                navArgument("code") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("error_code") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            ResetPasswordScreen(
                onPasswordUpdated = {
                    navController.navigate(Screen.Notebooks.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onCancelled = { navController.clearAndGoToLogin() },
                onRequestNewLink = {
                    navController.navigate(Screen.ForgotPassword.createRoute("")) {
                        popUpTo(Screen.ResetPassword.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(onBackToLogin = { navController.popBackStack(Screen.Login.route, inclusive = false) })
        }

        composable(Screen.Notebooks.route) {
            NotebooksScreen(
                onNavigateToDetail = { notebookTitle ->
                    navController.navigate(Screen.NotebookDetail.createRoute(notebookTitle))
                },
                onSignOut = sessionViewModel::signOut
            )
        }

        composable(
            route = Screen.NotebookDetail.route,
            arguments = listOf(
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title")?.let(Uri::decode)
                ?: stringResource(R.string.notebook_default_title)
            NotebookDetailScreen(
                notebookTitle = title,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }

    // After NavHost so the graph exists when a link from the launch intent is opened.
    HandleResetLinks(navController)
}

private fun NavController.clearAndGoToLogin() {
    navigate(Screen.Login.route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavController.backToLogin() {
    if (!popBackStack(Screen.Login.route, inclusive = false)) clearAndGoToLogin()
}

private const val RESET_LINK_SCHEME = "documind"
private const val RESET_LINK_HOST = "reset-password"

/**
 * Opens «New password» for documind://reset-password links, both from the launch intent and
 * from links that arrive while the app is open (singleTask). Each link is handled once, also
 * across rotation and process death, so a used code is not opened again.
 */
@Composable
private fun HandleResetLinks(navController: NavController) {
    val context = LocalContext.current
    var lastHandledLink by rememberSaveable { mutableStateOf<String?>(null) }
    DisposableEffect(navController, context) {
        var current = context
        while (current is ContextWrapper && current !is ComponentActivity) current = current.baseContext
        val activity = current as? ComponentActivity

        fun open(intent: Intent?) {
            val uri = intent?.data ?: return
            if (uri.scheme != RESET_LINK_SCHEME || uri.host != RESET_LINK_HOST) return
            val link = uri.toString()
            if (link == lastHandledLink) return
            lastHandledLink = link
            navController.navigate(
                Screen.ResetPassword.createRoute(uri.getQueryParameter("code"), uri.getQueryParameter("error_code"))
            ) { launchSingleTop = true }
        }

        open(activity?.intent)
        val listener = Consumer<Intent> { open(it) }
        activity?.addOnNewIntentListener(listener)
        onDispose { activity?.removeOnNewIntentListener(listener) }
    }
}
