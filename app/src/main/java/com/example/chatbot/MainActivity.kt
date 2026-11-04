package com.example.chatbot

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.chatbot.navigation.AppNavigation
import com.example.chatbot.ui.theme.ChatbotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always light, so the system bar icons must stay dark even in system dark mode.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // The detail screen has a white bottom bar that must reach the bottom edge.
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            ChatbotTheme {
                AppNavigation()
            }
        }
    }
}
