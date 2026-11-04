package com.example.chatbot

import androidx.compose.runtime.Composable
import com.example.chatbot.navigation.AppNavigation
import com.example.chatbot.ui.theme.ChatbotTheme

/**
 * Root of the shared UI. Platforms call it after [com.example.chatbot.di.initKoin].
 *
 * @param incomingLink the last link that opened the app, such as a password reset link.
 */
@Composable
fun App(incomingLink: String? = null) {
    ChatbotTheme {
        AppNavigation(incomingLink)
    }
}
