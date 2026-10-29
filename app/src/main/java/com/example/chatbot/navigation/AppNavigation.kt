package com.example.chatbot.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.chatbot.ui.screens.detail.NotebookDetailScreen
import com.example.chatbot.ui.screens.login.LoginScreen
import com.example.chatbot.ui.screens.notebooks.NotebooksScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Notebooks : Screen("notebooks")
    data object NotebookDetail : Screen("notebook_detail/{title}") {
        fun createRoute(title: String) = "notebook_detail/${Uri.encode(title)}"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Notebooks.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Notebooks.route) {
            NotebooksScreen(
                onNavigateToDetail = { notebookTitle ->
                    navController.navigate(Screen.NotebookDetail.createRoute(notebookTitle))
                }
            )
        }

        composable(
            route = Screen.NotebookDetail.route,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                    defaultValue = "Nuevo Cuaderno"
                }
            )
        ) { backStackEntry ->
            val title = backStackEntry.arguments?.getString("title") ?: "Nuevo Cuaderno"
            NotebookDetailScreen(
                notebookTitle = Uri.decode(title),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
