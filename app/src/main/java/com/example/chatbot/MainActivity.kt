package com.example.chatbot

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {

    // Link that opened the activity, e.g. documind://reset-password?code=… The shared UI decides
    // what to open and remembers which link it already handled.
    private var incomingLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is always light, so the system bar icons must stay dark even in system dark mode.
        val lightBars = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = lightBars, navigationBarStyle = lightBars)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // The detail screen has a white bottom bar that must reach the bottom edge.
            window.isNavigationBarContrastEnforced = false
        }
        incomingLink = intent?.dataString
        setContent {
            App(incomingLink = incomingLink)
        }
    }

    // launchMode="singleTask": links that arrive while the app is open come here.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingLink = intent.dataString
    }
}
