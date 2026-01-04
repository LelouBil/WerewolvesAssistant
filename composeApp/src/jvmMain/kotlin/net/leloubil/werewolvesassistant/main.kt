package net.leloubil.werewolvesassistant

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.leloubil.werewolvesassistant.ui.App
import net.leloubil.werewolvesassistant.modules.KoinApp
import org.koin.ksp.generated.startKoin

fun main() {
    KoinApp.startKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "WerewolvesAssistant",
        ) {
            App()
        }
    }
}
