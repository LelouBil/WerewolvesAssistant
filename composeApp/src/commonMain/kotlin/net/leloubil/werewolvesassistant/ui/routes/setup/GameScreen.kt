package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import arrow.core.Either
import arrow.core.raise.either
import com.composeunstyled.Icon
import com.composeunstyled.LocalContentColor
import com.composeunstyled.Text
import net.leloubil.werewolvesassistant.engine.ConfirmationStepPrompt
import net.leloubil.werewolvesassistant.engine.Game
import net.leloubil.werewolvesassistant.engine.GameEnd
import net.leloubil.werewolvesassistant.engine.GameStepData
import net.leloubil.werewolvesassistant.engine.GameStepPrompt
import net.leloubil.werewolvesassistant.engine.GameStepPromptChoosePlayer
import net.leloubil.werewolvesassistant.engine.PlayerName
import net.leloubil.werewolvesassistant.engine.Role
import net.leloubil.werewolvesassistant.engine.getPlayerNames
import net.leloubil.werewolvesassistant.ui.Carte
import net.leloubil.werewolvesassistant.ui.icons.MaterialSymbolsSkull
import net.leloubil.werewolvesassistant.ui.theme.Button
import net.leloubil.werewolvesassistant.ui.theme.Checkbox
import net.leloubil.werewolvesassistant.ui.theme.LocalAccentColorSet
import net.leloubil.werewolvesassistant.ui.theme.Theme
import org.jetbrains.compose.resources.pluralStringResource


fun <T> Either<Nothing, T>.infaillible(): T = when (this) {
    is Either.Right -> value
    is Either.Left -> value
}

interface ProcessPrompt {
    fun <D : GameStepData, P : GameStepPrompt<D, E>, E> processPrompt(game: Game, prompt: P, data: D): E?
    fun <D : ConfirmationStepPrompt.Info, P : ConfirmationStepPrompt<D>> processConfirm(game: Game, prompt: P) {
        processPrompt(game, prompt, prompt.getInfo(game))
    }

}

@Composable
fun GameScreen(game: Either<GameEnd, Game>, nextGame: (Either<GameEnd, Game>) -> Unit) {
    val prompt = game.map { it to it.nextPrompt }
    when (val et = prompt) {
        is Either.Left<GameEnd> -> {
            GameEnded(et.value)
        }

        is Either.Right<Pair<Game, GameStepPrompt<*, *>?>> -> {
            val (game, prompt) = et.value
            GameProcess(game, prompt, object : ProcessPrompt {
                override fun <D : GameStepData, P : GameStepPrompt<D, E>, E> processPrompt(
                    game: Game,
                    prompt: P,
                    data: D,
                ): E? {
                    val e = either {
                        game.removeLastPromptAndApply(data, prompt)
                    }
                    when (e) {
                        is Either.Left<E> -> return e.value
                        is Either.Right<Either<GameEnd, Game>> -> {
                            println("ha")
                            nextGame(e.value)
                            return null
                        }
                    }
                }

            })
        }
    }


}

@Composable
private fun GameEnded(et: GameEnd) {
    Text("Game ended with $et")
}

@Composable
private fun ThemeWrapper(game: Game, body: @Composable () -> Unit) {
    val isNight = game.steps.reversed().firstOrNull {
        it is GameStepPrompt.NightBegin.Info || it is GameStepPrompt.NightEnd.Info
    }?.let { it is GameStepPrompt.NightBegin.Info } ?: false

    println(isNight)
    val nightTransition = updateTransition(isNight)

    val backgroundColor by nightTransition.animateColor {
        if (it) {
            Color.Black
        } else {
            Color.Transparent
        }
    }

    Box(
        Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier.fillMaxSize().graphicsLayer {
//            blendMode = BlendMode.Difference
                alpha = 0.8f
            }.background(backgroundColor)
        ) {

        }
        val provided = if (isNight)
            arrayOf(
                LocalAccentColorSet provides Theme.colors.primary,
                LocalContentColor provides Theme.colors.secondary.content
            ) else emptyArray()

        CompositionLocalProvider(*provided) {
            body()
        }
    }
}

@Composable
fun GameProcess(
    game: Game,
    prompt: GameStepPrompt<*, *>?,
    promptProcessor: ProcessPrompt,
) = ThemeWrapper(game) {
    Column(
        Modifier.fillMaxWidth().padding(Theme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Theme.spacing.large)
    ) {
        if (prompt == null) {
            Text("null")
            return@ThemeWrapper
        }
        when (prompt) {
            is ConfirmationStepPrompt<*> -> {
                SimpleGameProcess(game, prompt) {
                    promptProcessor.processConfirm(game, prompt)
                }
            }

            is GameStepPromptChoosePlayer<*, *> -> {
                ChoosePlayerPrompt(prompt, game, promptProcessor)
            }

            is GameStepPrompt.CupidSetLovers -> Column {
                val lovers = remember { mutableStateListOf<PlayerName>() }

                Text("Cupid Set Lovers")

                game.players.forEach { p ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = lovers.contains(p),
                            onCheckedChange = { checked ->
                                if (checked) {
                                    lovers.add(p)
                                } else {
                                    lovers.remove(p)
                                }
                            },
                            enabled = lovers.contains(p) || lovers.size < 2
                        )
                        Text(p.name)
                    }
                }

                Button(
                    onClick = {
                        promptProcessor.processPrompt(
                            game,
                            prompt,
                            GameStepPrompt.CupidSetLovers.Data(lovers[0], lovers[1])
                        )
                    },
                    enabled = lovers.size == 2
                ) {
                    Text("Set Lovers")
                }
            }

            is GameStepPrompt.WitchStep -> WitchStep(promptProcessor, game, prompt)
        }
    }
}

@Composable
private fun <P : ConfirmationStepPrompt<D>, D : ConfirmationStepPrompt.Info> ColumnScope.SimpleGameProcess(
    game: Game,
    prompt: P,
    onConfirm: () -> Unit,
) {
    when (prompt) {
        GameStepPrompt.NightBegin -> {
            Text(
                "À destination de tous",
                Modifier.fillMaxWidth().padding(bottom = Theme.spacing.medium),
                Theme.typography.body.copy(textDecoration = TextDecoration.Underline)
            )
            Text("La nuit tombe sur le village. Tout le monde s'endort", style = Theme.typography.title)
        }

        GameStepPrompt.SeerShow -> {
            val info = prompt.getInfo(game)
            Text(
                "À destination de la voyante : ${game.getPlayerNames<Role.Seer>().joinToString(", ") { it.name }}",
                style = Theme.typography.body.copy(textDecoration = TextDecoration.Underline)
            )
            Text(
                "${info.player.name} est ${pluralStringResource(info.role.name, 1)}",
                Modifier.fillMaxWidth(),
                style = Theme.typography.title
            )

            Carte(Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(.5f), frontSide = info.role)
        }

        else -> {
            val info = prompt.getInfo(game)
            Text(prompt.toString())
            Text(info.toString())
//            Text(info.destination.toString())
        }

        // GameStepPrompt.NightEnd -> TODO()
        // is GameStepPrompt.DeathByLove -> TODO()
        // GameStepPrompt.GuardResurrect -> TODO()
        // GameStepPrompt.WitchShow -> TODO()
    }

    Button(onClick = onConfirm, Modifier.align(Alignment.CenterHorizontally)) {
        Text("Confirm")
    }
}

private enum class WitchAction {
    Heal,
    Kill,
    Skip
}

@Composable
private fun ColumnScope.WitchStep(
    promptProcessor: ProcessPrompt,
    game: Game,
    prompt: GameStepPrompt.WitchStep,
) {
    var action by remember { mutableStateOf<WitchAction?>(null) }

    Row {
        Button(
            onClick = {
                action = WitchAction.Heal
            },
            enabled = false // TODO trouver si joueur à soigner
        ) {
            Icon(Icons.Default.Add, null)
        }
        Button(
            onClick = {
                action = WitchAction.Kill
            },
            enabled = false // TODO afficher liste des joueurs à tuer
        ) {
            Icon(MaterialSymbolsSkull, null)
        }

        Button(
            onClick = {
                action = WitchAction.Skip
            }
        ) {
            Icon(Icons.Default.Close, null)
        }

        Text(action?.toString() ?: "No action selected")
    }

    Button(
        onClick = {
            when (action) {
                WitchAction.Heal -> {
                    // TODO data avec le joueur à soigner
//                    promptProcessor.processPrompt(
//                        game,
//                        prompt,
//                        GameStepPrompt.WitchStep.Data.Heal(/* playerName */)
//                    )
                }

                WitchAction.Kill -> {
                    // TODO data avec le joueur à tuer
//                    promptProcessor.processPrompt(
//                        game,
//                        prompt,
//                        GameStepPrompt.WitchStep.Data.Kill(/* playerName */)
//                    )
                }

                WitchAction.Skip -> {
                    promptProcessor.processPrompt(
                        game,
                        prompt,
                        GameStepPrompt.WitchStep.Data.Skip
                    )
                }

                null -> {
                    // no action selected
                }
            }
        },
        enabled = action != null
    ) {
        Text("Confirm")
    }
}

@Composable
private fun ColumnScope.ChoosePlayerPrompt(
    prompt: GameStepPromptChoosePlayer<*, *>,
    game: Game,
    promptProcessor: ProcessPrompt,
) {
    Text(prompt.toString())

    @Composable
    fun <P : GameStepPromptChoosePlayer<D, E>, D : GameStepData, E> PP(prompt: P, data: (PlayerName) -> D) =
        PlayerPicker(
            game,
            prompt.getValidPlayers(game),
            prompt,
            promptProcessor,
            data
        )
    when (prompt) {
        is GameStepPrompt.GuardProtect -> PP(prompt, GameStepPrompt.GuardProtect::Data)
        is GameStepPrompt.SeerSee -> PP(prompt, GameStepPrompt.SeerSee::Data)
        is GameStepPrompt.WerewolvesKill -> {
            Text(
                "À destination des loups-garous : ${
                    game.getPlayerNames<Role.CalledWithWolves>().joinToString(", ") { it.name }
                }",
                style = Theme.typography.body.copy(textDecoration = TextDecoration.Underline)
            )
            Text(
                "Les loups-garous se réveillent et choisissent une victime",
                style = Theme.typography.title
            )
            PP(prompt, GameStepPrompt.WerewolvesKill::Data)
        }

        is GameStepPrompt.WhiteWolfKill -> PP(prompt, GameStepPrompt.WhiteWolfKill::Data)
        is GameStepPrompt.HunterKill -> PP(prompt, GameStepPrompt.HunterKill::Data)
        is GameStepPrompt.MayorElection -> PP(prompt, GameStepPrompt.MayorElection::Data)
        is GameStepPrompt.VillagersKillVote -> PP(prompt, GameStepPrompt.VillagersKillVote::Data)
    }
}

@Composable
fun <P : GameStepPrompt<D, E>, D : GameStepData, E> ColumnScope.PlayerPicker(
    game: Game,
    x0: Set<PlayerName>,
    prompt: P,
    x1: ProcessPrompt,
    x2: (PlayerName) -> D,
) {
    Text("Sélectionner un joueur :")
    var error by remember { mutableStateOf<E?>(null) }
    error?.let { Text(it.toString()) }
    x0.forEach { playerName ->
        Button(
            onClick = {
                x1.processPrompt(game, prompt, x2(playerName))?.let {
                    error = it
                }
            },
            Modifier.defaultMinSize(minWidth = 128.dp).align(Alignment.CenterHorizontally)
        ) {
            Text(playerName.name)
        }
    }
}
