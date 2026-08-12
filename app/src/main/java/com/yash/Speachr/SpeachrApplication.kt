package com.yash.Speachr

import android.app.Application
import com.yash.Speachr.di.networkModule
import com.yash.Speachr.di.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SpeachrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@SpeachrApplication)
            modules(
                networkModule,
                repositoryModule,
            )
        }
    }
}
