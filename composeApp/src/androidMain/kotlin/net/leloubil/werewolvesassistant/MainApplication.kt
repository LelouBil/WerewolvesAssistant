package net.leloubil.werewolvesassistant

import android.app.Application
import net.leloubil.werewolvesassistant.modules.KoinApp
import org.koin.android.ext.koin.androidContext
import org.koin.ksp.generated.startKoin

class MainApplication: Application() {
    override fun onCreate() {
        KoinApp.startKoin {
            androidContext(this@MainApplication)
        }
    }
}
