@file:UseSerializers(
    EitherSerializer::class,
)

package net.leloubil.werewolvesassistant.ui

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import arrow.core.Either
import arrow.core.right
import arrow.core.serialization.EitherSerializer
import com.composeunstyled.Icon
import com.composeunstyled.ProgressBar
import com.composeunstyled.ProgressIndicator
import com.composeunstyled.Text
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.leloubil.werewolvesassistant.engine.Game
import net.leloubil.werewolvesassistant.engine.GameEnd
import net.leloubil.werewolvesassistant.engine.PlayerName
import net.leloubil.werewolvesassistant.engine.Role
import net.leloubil.werewolvesassistant.engine.RolesList
import net.leloubil.werewolvesassistant.modules.MusicService
import net.leloubil.werewolvesassistant.modules.MusicStatus
import net.leloubil.werewolvesassistant.modules.TrackMetadata
import net.leloubil.werewolvesassistant.modules.UrlTrack
import net.leloubil.werewolvesassistant.ui.routes.MainMenu
import net.leloubil.werewolvesassistant.ui.routes.setup.AssignRolesMenu
import net.leloubil.werewolvesassistant.ui.routes.setup.ChoosePlayersMenu
import net.leloubil.werewolvesassistant.ui.routes.setup.ChooseRolesMenu
import net.leloubil.werewolvesassistant.ui.routes.setup.GameScreen
import net.leloubil.werewolvesassistant.ui.routes.setup.PreGameShowRoles
import net.leloubil.werewolvesassistant.ui.theme.Button
import net.leloubil.werewolvesassistant.ui.theme.ColorSet
import net.leloubil.werewolvesassistant.ui.theme.LocalAccentColorSet
import net.leloubil.werewolvesassistant.ui.theme.ProvideContentColorSet
import net.leloubil.werewolvesassistant.ui.theme.background
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.time.Duration


@Serializable
sealed interface NavRoutes {
    @Serializable
    data object MainScreen : NavRoutes

    @Serializable
    sealed interface CreateGameScreens : NavRoutes

    @Serializable
    data object ChoosePlayersScreen : CreateGameScreens

    @Serializable
    data class ChooseRolesScreen(val players: List<PlayerName>) : CreateGameScreens

    @Serializable
    data class AssignRolesScreen(val players: List<PlayerName>, val roles: List<Role>) : CreateGameScreens

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

@Serializable
data class NavWrapper(val route: NavRoutes) : NavKey

val NavRoutes.navKey: NavWrapper
    get() = NavWrapper(this)

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {

        polymorphic(baseClass = NavKey::class) {
            subclass(serializer = NavWrapper.serializer())
        }
    }
}

@Composable
fun NavRoot(modifier: Modifier) {
    val backStack = rememberNavBackStack(config, NavRoutes.MainScreen.navKey)
    val onBack: () -> Unit = { backStack.removeLastOrNull() }
    val navigate: (NavRoutes) -> Unit = { backStack.add(it.navKey) }
    Column(modifier) {
        val topInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top)
        AppBar(modifier = Modifier.height(90.dp).windowInsetsPadding(topInsets)) {
            AppBarContents(onBack, backStack.size > 1)
        }
        SharedTransitionLayout {
            NavDisplay(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars.exclude(topInsets)),
                backStack = backStack,
                onBack = onBack,
                entryProvider = { key ->
                    when (val route = (key as NavWrapper).route) {
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
                                koinViewModel(key = route.toString()) { parametersOf(route.players) },
                                LocalNavAnimatedContentScope.current,
                                this@SharedTransitionLayout,
                                assignRoles = { players, roles ->
                                    navigate(
                                        NavRoutes.AssignRolesScreen(
                                            players,
                                            roles
                                        )
                                    )
                                })
                        }

                        is NavRoutes.AssignRolesScreen -> NavEntry(key) {
                            AssignRolesMenu(
                                koinViewModel(key = route.toString()) { parametersOf(route.players, route.roles) },
                                LocalNavAnimatedContentScope.current,
                                this@SharedTransitionLayout,
                                {
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
                                koinViewModel(key = route.toString()) {
                                    parametersOf(
                                        route.players,
                                        route.showingIndex
                                    )
                                },
                                nextShowIndex = {
                                    if (route.showingIndex == null) {
                                        navigate(
                                            NavRoutes.PreGameShowRolesScreen(
                                                route.players,
                                                0u
                                            )
                                        )
                                        return@PreGameShowRoles
                                    } else if (route.showingIndex + 1u >= route.players.size.toUInt()) {
                                        //all shown, go to game screen
                                        navigate(
                                            NavRoutes.GameScreen.GameScreenStart(
                                                route.players
                                            )
                                        )
                                        return@PreGameShowRoles
                                    } else {
                                        navigate(
                                            NavRoutes.PreGameShowRolesScreen(
                                                route.players,
                                                route.showingIndex + 1u
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
                            when (route) {
                                is NavRoutes.GameScreen.GameScreenStart -> GameScreen(
                                    Game(route.players)!!.right(),
                                    nextGame
                                )

                                is NavRoutes.GameScreen.GameScreenTurn -> GameScreen(route.game, nextGame)
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun RowScope.AppBarContents(
    onBack: () -> Unit,
    backEnabled: Boolean,
) {
    Button(onClick = onBack, enabled = backEnabled) {
        Text("Retour")
    }
    MusicPlayer()
}


@Composable
fun MusicProgressBar(
    progress: Float,
    isPaused: Boolean,
    colorSet: ColorSet = LocalAccentColorSet.current,
    modifier: Modifier,
) =
    ProvideContentColorSet(colorSet) {
        val animatableProgress = remember { Animatable(progress) }
        var previousProgress = progress
        LaunchedEffect(progress) {
            animatableProgress.animateTo(progress, animationSpec = tween(easing = LinearEasing))
        }

        LaunchedEffect(progress) {
            if (progress < previousProgress) {
                //reset to 0 instantly
                animatableProgress.snapTo(progress)
            }
            previousProgress = progress
        }

        LaunchedEffect(isPaused) {
            if (isPaused) {
                animatableProgress.snapTo(animatableProgress.value)
            }
        }
        val progressAnimated = animatableProgress.value
        val shape = RoundedCornerShape(100.dp)
        ProgressIndicator(
            progress = progressAnimated,
            modifier = modifier.clip(shape).background(colorSet.background),
            shape = shape,
        ) {
            ProgressBar()
        }
    }

@Composable
fun MusicPlayer() = Row(verticalAlignment = Alignment.CenterVertically) {
    val player = koinInject<MusicService>()
//    Text(text = "Music Player")
    val playerState by player.status.collectAsState()
//    Text(playerState.toString())

    when (val state = playerState) {
        is MusicStatus.HasMusic -> {
            val info = state.trackInfo
            val progress = state.progress
            val floatProgress = (progress.duration ?: Duration.ZERO) / info.duration
            Column {
                when (info.metadata) {
                    is TrackMetadata.FullMetadata -> {
                        Text(info.metadata.title, modifier = Modifier.align(Alignment.Start))
                    }
                }
                MusicProgressBar(
                    progress = floatProgress.toFloat().coerceIn(0f, 1f),
                    isPaused = progress is MusicStatus.Progress.Paused,
                    modifier = Modifier.height(10.dp).fillMaxWidth(0.5f)
                )
                fun format(d: Duration): String = d.toComponents { hours, minutes, seconds, nanoseconds ->
                    fun pad(num: Int): String = num.toString().padStart(2, '0')
                    if (hours > 0) {
                        "${pad(hours.toInt())}:${pad(minutes)}:${pad(seconds)}"
                    } else {
                        "${pad(minutes)}:${pad(seconds)}"
                    }
                }
                Text(
                    "${format(progress.duration ?: Duration.ZERO)} / ${format(info.duration)}",
                    modifier = Modifier.align(Alignment.End)
                )
            }
            Button(
                onClick = {
                    when (progress) {
                        is MusicStatus.Progress.Paused -> {
                            player.resume()
                        }

                        is MusicStatus.Progress.Playing -> {
                            player.pause()
                        }
                    }
                },
            ) {
                val icon = when (progress) {
                    is MusicStatus.Progress.Playing -> Icons.Default.Pause to "Pause"
                    is MusicStatus.Progress.Paused -> Icons.Default.PlayArrow to "Play"
                }
                Icon(icon.first, contentDescription = icon.second)
            }
        }

        MusicStatus.NoMusic -> {
            false
        }
    }

//    when (playerState) {
//        is MusicStatus.NoMusic -> {
    Button(onClick = {
        player.playReplacing(
            UrlTrack("https://www.audiocheck.net/Audio/audiocheck.net_welcome.mp3"),
            TrackMetadata.FullMetadata("audiotest lol")
        )
    }) {
        Text("Play")
    }
//        }
//
//        is MusicStatus.HasMusic -> {

//        }
//    }

}
