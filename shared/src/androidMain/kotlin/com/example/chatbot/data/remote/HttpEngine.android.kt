package com.example.chatbot.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

actual fun createHttpEngine(socketTimeout: Duration): HttpClientEngine = OkHttp.create {
    config {
        readTimeout(socketTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
        writeTimeout(socketTimeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)
    }
}
