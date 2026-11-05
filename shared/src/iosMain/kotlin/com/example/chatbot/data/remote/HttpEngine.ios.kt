package com.example.chatbot.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlin.time.Duration

// Not compiled on Windows (PLAN "Limitaciones del entorno").
actual fun createHttpEngine(socketTimeout: Duration): HttpClientEngine = Darwin.create {
    configureSession {
        // NSURLSession: longest wait for more data before the request fails.
        timeoutIntervalForRequest = socketTimeout.inWholeSeconds.toDouble()
    }
}
