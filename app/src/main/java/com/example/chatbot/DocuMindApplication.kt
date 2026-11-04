package com.example.chatbot

import android.app.Application
import com.example.chatbot.di.SupabaseConfig
import com.example.chatbot.di.initKoin
import org.koin.android.ext.koin.androidContext

class DocuMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(SupabaseConfig(url = BuildConfig.SUPABASE_URL, publishableKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY)) {
            androidContext(this@DocuMindApplication)
        }
    }
}
