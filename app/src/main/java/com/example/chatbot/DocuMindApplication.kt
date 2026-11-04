package com.example.chatbot

import android.app.Application
import com.example.chatbot.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DocuMindApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DocuMindApplication)
            modules(appModule)
        }
    }
}
