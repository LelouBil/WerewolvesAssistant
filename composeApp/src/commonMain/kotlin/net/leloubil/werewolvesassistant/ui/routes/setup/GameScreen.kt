package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import arrow.core.Either
import arrow.core.raise.either
import net.leloubil.werewolvesassistant.engine.*


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
fun GameProcess(
    game: Game,
    prompt: GameStepPrompt<*, *>?,
    promptProcessor: ProcessPrompt,
) {
    if (prompt == null) {
        Text("null")
        return
    }
    when (prompt) {
        is ConfirmationStepPrompt<*> -> Column {
            Text(prompt.toString())
            val info = prompt.getInfo(game)
            Text(info.toString())
            Text(info.destination.toString())
            Button(onClick = {
                println("lol")
                promptProcessor.processConfirm(game, prompt)
            }) {
                Text("Confirm")
            }

        }

        is GameStepPromptChoosePlayer<*, *> -> {
            ChoosePlayerPrompt(prompt, game, promptProcessor)
        }

        is GameStepPrompt.CupidSetLovers -> Box {
            val lovers = remember { mutableStateListOf<PlayerName>() }

            LazyColumn {

                item {
                    Text("Cupid Set Lovers")
                }

                items(game.players) { p ->
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

                item {
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
            }
        }

        is GameStepPrompt.WitchStep -> Column {
            Text("Witch Step") //todo
            Button(
                onClick = {
                    promptProcessor.processPrompt(game, prompt, GameStepPrompt.WitchStep.Data.Skip)
                }
            ) {
                Text("Skip")
            }
        }
    }
}

@Composable
private fun ChoosePlayerPrompt(
    prompt: GameStepPromptChoosePlayer<*, *>,
    game: Game,
    promptProcessor: ProcessPrompt,
) = Column {
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
        is GameStepPrompt.WerewolvesKill -> PP(prompt, GameStepPrompt.WerewolvesKill::Data)
        is GameStepPrompt.WhiteWolfKill -> PP(prompt, GameStepPrompt.WhiteWolfKill::Data)
        is GameStepPrompt.HunterKill -> PP(prompt, GameStepPrompt.HunterKill::Data)
        is GameStepPrompt.MayorElection -> PP(prompt, GameStepPrompt.MayorElection::Data)
        is GameStepPrompt.VillagersKillVote -> PP(prompt, GameStepPrompt.VillagersKillVote::Data)
    }
}

@Composable
fun <P : GameStepPrompt<D, E>, D : GameStepData, E> PlayerPicker(
    game: Game,
    x0: Set<PlayerName>,
    prompt: P,
    x1: ProcessPrompt,
    x2: (PlayerName) -> D,
) = Column {
    Text("PlayerPicker")
    var error by remember { mutableStateOf<E?>(null) }
    error?.let { Text(it.toString()) }
    x0.forEach { playerName ->
        Button(
            onClick = {
                x1.processPrompt(game, prompt, x2(playerName))?.let {
                    error = it
                }
            }
        ) {
            Text(playerName.toString())
        }
    }
}
