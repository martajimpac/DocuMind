package com.example.chatbot.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Collects one-shot events (navigation, dialogs) only while the screen is at least STARTED.
 * Events sent while the app is in the background wait in the ViewModel's buffered Channel and
 * are delivered once, when the screen is visible again. Main.immediate avoids losing an event
 * between the lifecycle change and the dispatch.
 */
@Composable
fun <T> ObserveAsEvents(events: Flow<T>, key: Any? = null, onEvent: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(events, lifecycleOwner.lifecycle, key) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            withContext(Dispatchers.Main.immediate) {
                events.collect(onEvent)
            }
        }
    }
}
