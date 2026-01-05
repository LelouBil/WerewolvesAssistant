package net.leloubil.werewolvesassistant.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import net.leloubil.werewolvesassistant.engine.Role
import net.leloubil.werewolvesassistant.ui.routes.MainMenu
import net.leloubil.werewolvesassistant.ui.routes.setup.ChoosePlayersMenu
import net.leloubil.werewolvesassistant.ui.routes.setup.ChooseRolesMenu
import net.leloubil.werewolvesassistant.ui.routes.setup.GameScreen
import net.leloubil.werewolvesassistant.ui.routes.setup.PreGameShowRoles
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

    @Serializable
    data class PreGameShowRolesScreen(val players: List<Pair<PlayerName, Role>>, val showingIndex: UInt?) :
        CreateGameScreens

    @Serializable
    data class GameScreen(val players: List<Pair<PlayerName, Role>>) : CreateGameScreens
}

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {}
}

@Composable
fun NavRoot() {
    val backStack = remember { mutableStateListOf<NavRoutes>(NavRoutes.MainScreen) }
    val onBack: () -> Unit = { backStack.removeLastOrNull() }
    val navigate: (NavRoutes) -> Unit = { backStack.add(it) }
    Column {
        Button(onClick = onBack) {
            Text("Retour")
        }
        NavDisplay(
            backStack = backStack,
            onBack = onBack,
            entryProvider = { key ->
                when (key) {
                    NavRoutes.MainScreen -> NavEntry(key) { MainMenu { navigate(NavRoutes.ChoosePlayersScreen) } }
                    NavRoutes.ChoosePlayersScreen -> NavEntry(key) {
                        ChoosePlayersMenu({
                            navigate(
                                NavRoutes.ChooseRolesScreen(
                                    it
                                )
                            )
                        })
                    }

                    is NavRoutes.ChooseRolesScreen -> NavEntry(key) {
                        ChooseRolesMenu(
                            koinViewModel { parametersOf(key.players) },
                            preGame = {
                                navigate(
                                    NavRoutes.PreGameShowRolesScreen(
                                        it,
                                        null
                                    )
                                )
                            })
                    }

                    is NavRoutes.PreGameShowRolesScreen -> NavEntry(key) {
                        PreGameShowRoles(
                            koinViewModel { parametersOf(key.players, key.showingIndex) },
                            nextShowIndex = {
                                if (key.showingIndex == null) {
                                    navigate(
                                        NavRoutes.PreGameShowRolesScreen(
                                            key.players,
                                            0u
                                        )
                                    )
                                    return@PreGameShowRoles
                                } else if (key.showingIndex + 1u >= key.players.size.toUInt()) {
                                    //all shown, go to game screen
                                    navigate(
                                        NavRoutes.GameScreen(
                                            key.players
                                        )
                                    )
                                    return@PreGameShowRoles
                                } else {
                                    navigate(
                                        NavRoutes.PreGameShowRolesScreen(
                                            key.players,
                                            key.showingIndex + 1u
                                        )
                                    )
                                }
                            },

                            )
                    }

                    is NavRoutes.GameScreen -> NavEntry(key) {
                        GameScreen(koinViewModel { parametersOf(key.players) })
                    }
                }
            }
        )
    }
}
