package com.example.chatbot.di

import com.example.chatbot.data.remote.createHttpEngine
import com.example.chatbot.data.repository.SupabaseAuthRepository
import com.example.chatbot.data.repository.SupabaseNotebookRepository
import com.example.chatbot.domain.repository.AuthRepository
import com.example.chatbot.domain.repository.NotebookRepository
import com.example.chatbot.domain.validation.CredentialsValidator
import com.example.chatbot.domain.validation.PdfValidator
import com.example.chatbot.navigation.SessionViewModel
import com.example.chatbot.ui.screens.forgotpassword.ForgotPasswordViewModel
import com.example.chatbot.ui.screens.detail.NotebookDetailViewModel
import com.example.chatbot.ui.screens.login.LoginViewModel
import com.example.chatbot.ui.screens.notebooks.NotebooksViewModel
import com.example.chatbot.ui.screens.register.RegisterViewModel
import com.example.chatbot.ui.screens.resetpassword.ResetPasswordViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// Upload limits (PLAN "Ajuste tras la validación"): 20 MB at about 1 Mbps takes ~160 s.
// SOCKET_TIMEOUT is the longest a single read or write may stall.
private val SOCKET_TIMEOUT = 60.seconds
private val UPLOAD_TIMEOUT = 5.minutes

/** Supabase project settings. Each platform passes its own; only the public publishable key, never the service role key. */
data class SupabaseConfig(val url: String, val publishableKey: String)

/** Starts Koin with the shared modules. Platforms add their own setup (for example the Android context) in [platform]. */
fun initKoin(config: SupabaseConfig, platform: KoinAppDeclaration = {}) {
    startKoin {
        platform()
        modules(appModule(config))
    }
}

fun appModule(config: SupabaseConfig): Module = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = config.url,
            supabaseKey = config.publishableKey,
        ) {
            // The default engine stops a read or write that stalls for 10 s, which breaks PDF uploads on
            // slow mobile networks. Other calls keep supabase-kt's 10 s total request timeout.
            httpEngine = createHttpEngine(SOCKET_TIMEOUT)
            install(Auth) { flowType = FlowType.PKCE }
            install(Postgrest)
            install(Storage) { transferTimeout = UPLOAD_TIMEOUT }
        }
    }
    single<AuthRepository> { SupabaseAuthRepository(get<SupabaseClient>().auth) }
    factoryOf(::CredentialsValidator)
    factoryOf(::PdfValidator)
    single<NotebookRepository> {
        val client = get<SupabaseClient>()
        SupabaseNotebookRepository(client.postgrest, client.storage, client.auth, get())
    }

    viewModelOf(::SessionViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
    viewModelOf(::NotebooksViewModel)
    viewModelOf(::NotebookDetailViewModel)
}
