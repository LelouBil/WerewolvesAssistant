@file:UseSerializers(
    EitherSerializer::class,
)

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
import arrow.core.Either
import arrow.core.right
import arrow.core.serialization.EitherSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.modules.SerializersModule
import net.leloubil.werewolvesassistant.engine.Game
import net.leloubil.werewolvesassistant.engine.GameEnd
import net.leloubil.werewolvesassistant.engine.PlayerName
import net.leloubil.werewolvesassistant.engine.RolesList
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
    data class PreGameShowRolesScreen(val players: RolesList, val showingIndex: UInt?) :
        CreateGameScreens

    @Serializable
    sealed interface GameScreen : CreateGameScreens {
        @Serializable
        data class GameScreenStart(val players: RolesList) : GameScreen

        @Serializable
        data class GameScreenTurn(val game: Either<GameEnd, Game>) : GameScreen
    }

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
        if (backStack.size > 1) {
            Button(onClick = onBack) {
                Text("Retour")
            }
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
                            koinViewModel(key = key.toString()) { parametersOf(key.players) },
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
                        println(key)
                        PreGameShowRoles(
                            koinViewModel(key = key.toString()) { parametersOf(key.players, key.showingIndex) },
                            nextShowIndex = {
                                if (key.showingIndex == null) {
                                    println("ShowingIndex null")
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
                                        NavRoutes.GameScreen.GameScreenStart(
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
                        val nextGame: (Either<GameEnd, Game>) -> Unit = {
                            println("navigating: $it")
                            navigate(NavRoutes.GameScreen.GameScreenTurn(it))
                        }
                        when (key) {
                            is NavRoutes.GameScreen.GameScreenStart -> GameScreen(Game(key.players)!!.right(), nextGame)
                            is NavRoutes.GameScreen.GameScreenTurn -> GameScreen(key.game, nextGame)
                        }
                    }
                }
            }
        )
    }
}
