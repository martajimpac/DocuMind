package com.example.chatbot.data.remote

import io.ktor.client.engine.HttpClientEngine
import kotlin.time.Duration

/** Platform HTTP engine whose reads and writes may stall for up to [socketTimeout]. */
expect fun createHttpEngine(socketTimeout: Duration): HttpClientEngine
