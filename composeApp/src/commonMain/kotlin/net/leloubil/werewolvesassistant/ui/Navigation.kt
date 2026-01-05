package net.leloubil.werewolvesassistant.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import net.leloubil.werewolvesassistant.engine.PlayerName
import net.leloubil.werewolvesassistant.ui.routes.MainMenu
import net.leloubil.werewolvesassistant.ui.routes.setup.ChoosePlayersMenu
import net.leloubil.werewolvesassistant.ui.routes.setup.ChooseRolesMenu
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Serializable
sealed interface NavRoutes : NavKey {
    @Serializable
    data object MainScreen : NavRoutes

    @Serializable
    sealed interface CreateGameScreens : NavRoutes

    @Serializable
    data object ChoosePlayersScreen : CreateGameScreens

    @Serializable
    data class ChooseRolesScreen(val players: List<PlayerName>) : CreateGameScreens
}

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {}
}

@Composable
fun NavRoot() {
    val backStack = remember { mutableStateListOf<NavRoutes>(NavRoutes.MainScreen) }
    val onBack: () -> Unit = { backStack.removeLastOrNull() }
    val navigate: (NavRoutes) -> Unit = { backStack.add(it) }
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryProvider = { key ->
            when (key) {
                NavRoutes.MainScreen -> NavEntry(key) {MainMenu({ navigate(NavRoutes.ChoosePlayersScreen) })}
                NavRoutes.ChoosePlayersScreen -> NavEntry(key) { ChoosePlayersMenu({ navigate(NavRoutes.ChooseRolesScreen(it)) })}
                is NavRoutes.ChooseRolesScreen -> NavEntry(key) {ChooseRolesMenu(koinViewModel { parametersOf(key.players)})}
            }
        }
    )
}
