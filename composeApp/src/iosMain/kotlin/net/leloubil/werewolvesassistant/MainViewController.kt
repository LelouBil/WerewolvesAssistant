package net.leloubil.werewolvesassistant

import androidx.compose.ui.window.ComposeUIViewController
import net.leloubil.werewolvesassistant.ui.App
import net.leloubil.werewolvesassistant.modules.KoinApp
import org.koin.ksp.generated.startKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    KoinApp.startKoin()
    return ComposeUIViewController { App() }
}
