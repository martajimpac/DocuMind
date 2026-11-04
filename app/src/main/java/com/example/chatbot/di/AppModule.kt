package com.example.chatbot.di

import com.example.chatbot.BuildConfig
import com.example.chatbot.data.repository.SupabaseAuthRepository
import com.example.chatbot.domain.repository.AuthRepository
import com.example.chatbot.domain.validation.CredentialsValidator
import com.example.chatbot.navigation.SessionViewModel
import com.example.chatbot.ui.screens.forgotpassword.ForgotPasswordViewModel
import com.example.chatbot.ui.screens.login.LoginViewModel
import com.example.chatbot.ui.screens.register.RegisterViewModel
import com.example.chatbot.ui.screens.resetpassword.ResetPasswordViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    single<SupabaseClient> {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY,
        ) {
            install(Auth) { flowType = FlowType.PKCE }
        }
    }
    single<AuthRepository> { SupabaseAuthRepository(get<SupabaseClient>().auth) }
    factoryOf(::CredentialsValidator)

    viewModelOf(::SessionViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::ResetPasswordViewModel)
}
