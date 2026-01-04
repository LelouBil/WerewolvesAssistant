package net.leloubil.werewolvesassistant.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import net.leloubil.werewolvesassistant.ui.routes.CreateGameMenu
import net.leloubil.werewolvesassistant.ui.routes.MainMenu

@Serializable
sealed interface NavRoutes: NavKey {
    @Serializable
    data object MainScreen : NavRoutes
    @Serializable
    data object CreateGameScreen : NavRoutes
}

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {}
}

@Composable
fun NavRoot() {
    val backStack = remember { mutableStateListOf<NavRoutes>(NavRoutes.MainScreen) }
    val onBack: () -> Unit = { backStack.removeLastOrNull() }
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryProvider = { key ->
            when (key) {
                NavRoutes.MainScreen -> NavEntry(key) { MainMenu({backStack.add(NavRoutes.CreateGameScreen)}) }
                NavRoutes.CreateGameScreen -> NavEntry(key) {CreateGameMenu()}
            }
        }
    )
}
