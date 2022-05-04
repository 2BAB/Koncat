package me.xx2bab.koncat.sample

import android.app.Application
import me.xx2bab.koncat.runtime.Koncat
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
//            androidLogger()
            androidContext(this@App)
            modules(Koncat().getTypedProperties(Module::class) ?: listOf())
        }
    }
}