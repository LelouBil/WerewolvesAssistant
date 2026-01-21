package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
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
import net.leloubil.werewolvesassistant.engine.*
import net.leloubil.werewolvesassistant.engine.GameStepPrompt.WitchStep.Data.Kill
import net.leloubil.werewolvesassistant.modules.BackgroundMusicPlayer
import net.leloubil.werewolvesassistant.modules.MusicService
import net.leloubil.werewolvesassistant.ui.CardSide
import net.leloubil.werewolvesassistant.ui.Carte
import net.leloubil.werewolvesassistant.ui.components.MusicPlayer
import net.leloubil.werewolvesassistant.ui.components.SoundsMusic
import net.leloubil.werewolvesassistant.ui.components.SoundsSFX
import net.leloubil.werewolvesassistant.ui.icons.MaterialSymbolsSkull
import net.leloubil.werewolvesassistant.ui.theme.Button
import net.leloubil.werewolvesassistant.ui.theme.Checkbox
import net.leloubil.werewolvesassistant.ui.theme.LocalAccentColorSet
import net.leloubil.werewolvesassistant.ui.theme.Theme
import org.jetbrains.compose.resources.pluralStringResource
import org.koin.compose.koinInject
import org.koin.core.qualifier.named


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
                        is Either.Left<E> -> return e.value.also {
                            println("error: $it")
                        }

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
private fun GameEnded(et: GameEnd) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Theme.spacing.medium)
) {
    Text("Game ended with $et")
    val bgPlayer = koinInject<MusicService>(named<BackgroundMusicPlayer>())

    MusicPlayer(if (et is GameEnd.VillagersWon) SoundsSFX.VICTORY else SoundsSFX.DEFEAT) {
        bgPlayer.pause()
    }
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

        @Suppress("SpreadOperator")
        CompositionLocalProvider(*provided) {
            body()
        }
    }
}


@Composable
fun InfoHeader(
    destinationName: String,
    destinations: List<PlayerName>?,
    title: String? = null,
    modifier: Modifier = Modifier,
) = Column(
    Modifier.fillMaxWidth().then(modifier),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(Theme.spacing.medium)
) {
    Text(
        "$destinationName${destinations?.let { " : " + it.joinToString(", ") { it.name } } ?: ""}",
        style = Theme.typography.body.copy(textDecoration = TextDecoration.Underline)
    )
    if (title != null) {
        Text(
            title,
            Modifier.fillMaxWidth(),
            style = Theme.typography.title
        )
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


            is GameStepPrompt.CupidSetLovers -> Column(
                verticalArrangement = Arrangement.spacedBy(Theme.spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val lovers = remember { mutableStateListOf<PlayerName>() }
                var validPair: Pair<PlayerName, PlayerName>? by remember { mutableStateOf(null) }

                InfoHeader("À destination de cupidon", game.getPlayerNames<Role.Cupid>(), "Choisissez les amoureux")

                AnimatedContent(validPair) { p ->
                    Column(verticalArrangement = Arrangement.spacedBy(Theme.spacing.medium)) {
                        if (p == null) {
                            game.players.forEach { p ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Checkbox(
                                        modifier = Modifier.size(40.dp),
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
                                    Text(
                                        p.name,
                                        modifier = Modifier.padding(horizontal = Theme.spacing.medium),
                                        style = Theme.typography.buttonTitle
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    validPair = lovers.first() to lovers.last()

                                },
                                enabled = lovers.size == 2
                            ) {
                                Text("Valider")
                            }
                        } else {
                            val (lover1, lover2) = p
                            Text(
                                "${lover1.name} et ${lover2.name} sont amoureux",
                                style = Theme.typography.title
                            )

                            Row {
                                Button(
                                    onClick = {
                                        promptProcessor.processPrompt(
                                            game,
                                            prompt,
                                            GameStepPrompt.CupidSetLovers.Data(lover1, lover2)
                                        )
                                    }
                                ) {
                                    Text("Confirmer")
                                }
                                Button(
                                    onClick = {
                                        validPair = null
                                    }
                                ) {
                                    Text("Annuler")
                                }
                            }
                        }
                    }
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

            MusicPlayer(listOf(SoundsMusic.MIDNIGHT, SoundsMusic.MOONLIGHT, SoundsMusic.TREASON).random())
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

            Carte(Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(.5f), front = info.role)
        }

        GameStepPrompt.NightEnd -> {
            val info = prompt.getInfo(game)
            val text = when (info.killed.size) {
                0 -> "Personne n'est mort cette nuit"
                1 -> "1 personne est morte cette nuit"
                else -> "${info.killed.size} personnes sont mortes cette nuit"
            }
            Text(text, style = Theme.typography.title, modifier = Modifier.align(Alignment.CenterHorizontally))
            MusicPlayer(SoundsMusic.HARP)
        }

        is GameStepPrompt.DeadShowRole -> {
            val info = prompt.getInfo(game)
            var currentShownIndex: Int? by remember { mutableStateOf(null) }
            val currentShownPlayer by derivedStateOf { currentShownIndex?.let { info.players[it] } }
            AnimatedContent(currentShownPlayer, modifier = Modifier.align(Alignment.CenterHorizontally)) { shown ->
                if (shown == null) {
                    Button(
                        onClick = {
                            currentShownIndex = 0
                        }
                    ) {
                        Text("Révéler les morts")
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Theme.spacing.medium)
                    ) {
                        val (player, role) = shown
                        RevealDeadComponent(player, role)
                        if (currentShownIndex != null && currentShownIndex!! + 1 < info.players.size) {
                            Button(
                                onClick = {
                                    currentShownIndex = (currentShownIndex ?: 0) + 1
                                }
                            ) {
                                Text("Continuer")
                            }
                        }
                    }
                }
            }
        }

        GameStepPrompt.GuardResurrect -> {
            val info = prompt.getInfo(game)
            if (info.resurrected != null) {
                Text(
                    info.resurrected.name + " est protégé par le garde, mais personne ne le sait encore...",
                    style = Theme.typography.buttonTitle
                )
            } else {
                Text("Le personne protégée par le garde n'a rien subi", style = Theme.typography.buttonTitle)
            }
        }

        GameStepPrompt.WitchShow -> {
            val info = prompt.getInfo(game)
            Text(
                "À destination de la sorcière : ${game.getPlayerNames<Role.Seer>().joinToString(", ") { it.name }}",
                style = Theme.typography.body.copy(textDecoration = TextDecoration.Underline)
            )
            if (info.killedByWolves != null) {
                Text(
                    "La victime des loups est ${info.killedByWolves.name}",
                    Modifier.fillMaxWidth(),
                    style = Theme.typography.title
                )
            } else {
                Text(
                    "Personne n'est mort par les loups-garous (mais il faut seulement le dire a la sorcière)",
                    Modifier.fillMaxWidth(),
                    style = Theme.typography.title)
            }
        }

        is GameStepPrompt.DeathByLove -> {
            val info = prompt.getInfo(game)
            Text("${info.dead.name} était amoureux de ${info.cause.name}", style = Theme.typography.title)
            Text("${info.dead.name} est mort de chagrin")
        }
    }

    Button(onClick = onConfirm, Modifier.align(Alignment.CenterHorizontally)) {
        Text("Confirm")
    }
}


@Composable
fun RevealDeadComponent(player: PlayerName, role: Role) {
    Text(player.name + " est mort", style = Theme.typography.title)
    Text("il/elle etait", style = Theme.typography.buttonTitle)

    var revealed by remember { mutableStateOf(false) }

    Carte(
        Modifier.fillMaxWidth(.5f).clickable {
            revealed = !revealed
        }, front = role, wantedSide =
            if (revealed) CardSide.FrontSide else CardSide.BackSide
    )

    AnimatedVisibility(revealed) {
        Text(pluralStringResource(role.name, 1), style = Theme.typography.buttonTitle)
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
    InfoHeader("À destination de la sorcière", game.getPlayerNames<Role.Witch>(), "Choisissez une action :")
    var action by remember { mutableStateOf<WitchAction?>(null) }
    val killedPlayer by derivedStateOf {
        game.thisNight().filterIsInstance<GameStepPrompt.WitchShow.Info>().firstOrNull()?.killedByWolves
    }
    val alreadyKilled by derivedStateOf {
        game.steps.filterIsInstance<GameStepPrompt.WitchStep.Data.Kill>().any()
    }
    val alreadyHealed by derivedStateOf {
        game.steps.filterIsInstance<GameStepPrompt.WitchStep.Data.Heal>().any()
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(Theme.spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    ) {
        Button(
            onClick = {
                action = WitchAction.Heal
            },
            enabled = killedPlayer != null && !alreadyHealed
        ) {
            Icon(Icons.Default.Add, null)
        }
        Button(
            onClick = {
                action = WitchAction.Kill
            },
            enabled = !alreadyKilled
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
    }

    AnimatedContent(action, modifier = Modifier.align(Alignment.CenterHorizontally)) {

        when (action) {
            WitchAction.Kill -> {
                PlayerPickerBase(
                    "Choisissez votre victime",
                    game.players.filter { game.getLivingState(it) is Game.LivingState.Alive }.toSet()
                ) {
                    promptProcessor.processPrompt(game, prompt, Kill(it))
                }
            }

            WitchAction.Heal -> Column(
                verticalArrangement = Arrangement.spacedBy(Theme.spacing.medium),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Voulez vous sauver ${killedPlayer!!.name} ?", style = Theme.typography.title)
                if (killedPlayer != null) {
                    Button(
                        onClick = {
                            promptProcessor.processPrompt(
                                game,
                                prompt,
                                GameStepPrompt.WitchStep.Data.Heal(killedPlayer!!)
                            )
                        }
                    ) {
                        Text("Confirmer")
                    }
                }
            }

            WitchAction.Skip -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Theme.spacing.medium)
            ) {
                Text("Ne rien faire", style = Theme.typography.buttonTitle)
                Button(
                    onClick = {

                        promptProcessor.processPrompt(
                            game,
                            prompt,
                            GameStepPrompt.WitchStep.Data.Skip
                        )
                    }) {
                    Text("Confirmer")
                }
            }

            null -> {

            }
        }
    }
}

@Composable
private fun ColumnScope.ChoosePlayerPrompt(
    prompt: GameStepPromptChoosePlayer<*, *>,
    game: Game,
    promptProcessor: ProcessPrompt,
) {
    @Composable
    fun <P : GameStepPromptChoosePlayer<D, E>, D : GameStepData, E> PP(
        text: String,
        prompt: P,
        data: (PlayerName) -> D,
    ) = PlayerPicker(
        text,
        game,
        prompt.getValidPlayers(game),
        prompt,
        promptProcessor,
        data
    )
    when (prompt) {
        is GameStepPrompt.GuardProtect -> {
            InfoHeader(
                "À destination du salvateur",
                game.getPlayerNames<Role.Cupid>()
            )
            PP("Choisissez la personne a protéger", prompt, GameStepPrompt.GuardProtect::Data)
        }

        is GameStepPrompt.SeerSee -> {

            InfoHeader("À destination de la voyante", game.getPlayerNames<Role.Seer>())
            PP("Choisissez qui observer", prompt, GameStepPrompt.SeerSee::Data)
        }

        is GameStepPrompt.WerewolvesKill -> {

            InfoHeader(
                "À destination des loups-garous",
                game.getPlayerNames<Role.CalledWithWolves>(),
                "Les loups-garous se réveillent et choisissent une victime"
            )
            MusicPlayer(SoundsSFX.WEREWOLVES_TIME)
            PP("Choisissez votre victime", prompt, GameStepPrompt.WerewolvesKill::Data)
        }

        is GameStepPrompt.WhiteWolfKill -> {
            PP("Choisissez votre victime", prompt, GameStepPrompt.WhiteWolfKill::Data)
        }

        is GameStepPrompt.HunterKill -> {
            InfoHeader("À destination du chasseur", game.getPlayerNames<Role.Hunter>())
            Text("Tout le monde se tait !", style = Theme.typography.title)
            //todo chrono
            MusicPlayer(SoundsSFX.SHOTGUN)
            PP("Choisissez votre victime", prompt, GameStepPrompt.HunterKill::Data)
        }

        is GameStepPrompt.MayorElection -> {
            InfoHeader("À destination du village", null)
            //todo chronometre de campagne et chronometre de vote
            MusicPlayer(listOf(SoundsMusic.PRESSURE, SoundsMusic.TREASON, SoundsMusic.WHO_SHOULD_WE_KILL).random())
            PP("Choisissez le nouveau maire", prompt, GameStepPrompt.MayorElection::Data)
        }

        is GameStepPrompt.VillagersKillVote -> {
            InfoHeader("À destination du village", null)
            //todo chrono
            MusicPlayer(listOf(SoundsMusic.PRESSURE, SoundsMusic.TREASON, SoundsMusic.WHO_SHOULD_WE_KILL).random())
            PP("Choisissez votre victime", prompt, GameStepPrompt.VillagersKillVote::Data)
        }
    }
}


@Composable
fun PlayerPickerBase(
    text: String,
    x0: Set<PlayerName>,
    onPick: (PlayerName) -> Unit,
) {
    var selected: PlayerName? by remember { mutableStateOf(null) }

    AnimatedContent(selected) { curselected ->
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

            Text(text, style = Theme.typography.buttonTitle)
            if (curselected == null) {
                x0.forEach { playerName ->
                    Button(
                        onClick = {
                            selected = playerName
                        },
                        Modifier.defaultMinSize(minWidth = 128.dp).align(Alignment.CenterHorizontally)
                    ) {
                        Text(playerName.name)
                    }
                }
            } else {
                Text(
                    "Confirmer: ${curselected.name}",
                    Modifier.align(Alignment.CenterHorizontally).padding(Theme.spacing.large),
                    style = Theme.typography.title
                )
                Row(
                    Modifier.defaultMinSize(minWidth = 128.dp).align(Alignment.CenterHorizontally),
                ) {
                    Button(
                        onClick = {
                            onPick(curselected)
                        },
                        Modifier.defaultMinSize(minWidth = 64.dp)
                    ) {
                        Text(curselected.name)
                    }
                    Button(onClick = { selected = null }, Modifier.defaultMinSize(minWidth = 64.dp)) {
                        Text("Annuler")
                    }
                }
            }
        }
    }
}


@Composable
fun <P : GameStepPrompt<D, E>, D : GameStepData, E> PlayerPicker(
    text: String,
    game: Game,
    x0: Set<PlayerName>,
    prompt: P,
    x1: ProcessPrompt,
    x2: (PlayerName) -> D,
) {
    var error by remember { mutableStateOf<E?>(null) }
    error?.let { Text(it.toString()) }

    PlayerPickerBase(
        text,
        x0,
        onPick = {
            x1.processPrompt(game, prompt, x2(it))?.let {
                error = it
            }
        }
    )


}
