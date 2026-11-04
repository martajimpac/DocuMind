package com.example.chatbot

import androidx.compose.ui.window.ComposeUIViewController
import com.example.chatbot.di.SupabaseConfig
import com.example.chatbot.di.initKoin
import platform.UIKit.UIViewController

/**
 * iOS entry point for the shared UI. Call it once from the SwiftUI/UIKit app, after reading the
 * Supabase settings from the app configuration. Not built or run yet: it needs macOS with Xcode.
 */
fun MainViewController(config: SupabaseConfig): UIViewController {
    initKoin(config)
    return ComposeUIViewController { App() }
}
