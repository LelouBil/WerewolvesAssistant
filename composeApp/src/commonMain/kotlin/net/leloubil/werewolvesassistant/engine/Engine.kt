package net.leloubil.werewolvesassistant.engine

import arrow.core.Either
import arrow.core.raise.Raise
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.PluralStringResource
import werewolvesassistant.composeapp.generated.resources.Res
import werewolvesassistant.composeapp.generated.resources.role_cupid
import werewolvesassistant.composeapp.generated.resources.role_guard
import werewolvesassistant.composeapp.generated.resources.role_hunter
import werewolvesassistant.composeapp.generated.resources.role_seer
import werewolvesassistant.composeapp.generated.resources.role_villager
import werewolvesassistant.composeapp.generated.resources.role_werewolf
import werewolvesassistant.composeapp.generated.resources.role_white_wolf
import werewolvesassistant.composeapp.generated.resources.role_witch


sealed class GameStepPromptChoosePlayer<T : GameStepData, E> : GameStepPrompt<T, E>() {
    protected abstract fun createPrompt(player: PlayerName): T

    fun getValidPlayers(game: Game): Set<PlayerName> {
        return game.players.filter {
            this.checkStepData(game, this.createPrompt(it)) == null && game.getLivingState(it) is Game.LivingState.Alive
        }.toSet()
    }
}

sealed interface Destination {
    data object All : Destination
    data object None : Destination

    data class Specific(val roles: Set<Role>) : Destination {
        constructor(vararg roles: Role) : this(roles.toSet())
    }
}

sealed class ConfirmationStepPrompt<I : ConfirmationStepPrompt.Info> :
    GameStepPrompt<I, Nothing>() {

    interface Info : GameStepData {
        val destination: Destination
    }

    override fun checkStepData(game: Game, data: I): Nothing? = null
    abstract fun getInfo(game: Game): I

    context(_: Raise<Nothing>)
    fun process(game: Game): Either<GameEnd, Game> {
        return game.removeLastPromptAndApply(getInfo(game), this)
    }
}


@Serializable
sealed interface Role {
    val name: PluralStringResource

    sealed interface Team {
        sealed interface WinsWithVillagers : Team
        sealed interface WinsWithWolves : Team
    }

    sealed interface CalledWithWolves : Role

    data object SimpleVillager : Role, Team.WinsWithVillagers {
        override val name: PluralStringResource = Res.plurals.role_villager
    }

    data object Cupid : Role, Team.WinsWithVillagers {
        override val name: PluralStringResource = Res.plurals.role_cupid
    }

    data object Seer : Role, Team.WinsWithVillagers {
        override val name: PluralStringResource = Res.plurals.role_seer
    }

    data object Witch : Role, Team.WinsWithVillagers {
        override val name: PluralStringResource = Res.plurals.role_witch
    }

    data object Guard : Role, Team.WinsWithVillagers {
        override val name: PluralStringResource = Res.plurals.role_guard
    }

    data object Hunter : Role, Team.WinsWithVillagers {
        override val name: PluralStringResource = Res.plurals.role_hunter
    }

    data object Werewolf : Role, Team.WinsWithWolves, CalledWithWolves {
        override val name: PluralStringResource = Res.plurals.role_werewolf
    }

    data object WhiteWolf : Role, CalledWithWolves {
        override val name: PluralStringResource = Res.plurals.role_white_wolf
    }

}


sealed interface GameStepData {
    companion object;
    val checkGameEnd: Boolean get() = false

    interface MarksPublicKilled : GameStepData {
        val killed: Set<PlayerName>
        override val checkGameEnd: Boolean get() = true
    }

    interface NightHiddenKill : GameStepData {
        val hiddenKilled: Set<PlayerName>
    }

    interface MarksAlive : GameStepData {
        val alive: Set<PlayerName>
    }

    interface SetsRole : GameStepData {
        val assignments: Map<PlayerName, List<Role>>
    }

}

@Serializable
sealed class GameStepPrompt<T : GameStepData, E> {
    abstract fun exists(game: Game): Boolean

    abstract fun checkStepData(game: Game, data: T): E?

    context(_: Raise<E>)
    fun process(game: Game, data: T): Either<GameEnd, Game> {
        return game.removeLastPromptAndApply(data, this)
    }


    data object NightBegin : ConfirmationStepPrompt<NightBegin.Info>() {
        data object Info : ConfirmationStepPrompt.Info {
            override val destination: Destination = Destination.All
        }

        override fun exists(game: Game): Boolean = true
        override fun getInfo(game: Game): Info = Info
    }

    data object NightEnd : ConfirmationStepPrompt<NightEnd.Info>() {
        data class Info(val deathsSummary: List<Pair<PlayerName, List<Role>>>) : ConfirmationStepPrompt.Info,
            GameStepData.MarksPublicKilled {
            override val destination: Destination = Destination.All

            override val killed: Set<PlayerName> = deathsSummary.map { it.first }.toSet()
        }

        override fun exists(game: Game): Boolean = true
        override fun getInfo(game: Game): Info {
            val summary = game.thisNight().fold(emptyList<PlayerName>()) { acc, step ->
                when (step) {
                    is GameStepData.NightHiddenKill -> acc + step.hiddenKilled
                    is GameStepData.MarksPublicKilled -> acc + step.killed
                    is GameStepData.MarksAlive -> acc - step.alive
                    else -> acc
                }
            }.map { it to game.getRoles(it) }

            return Info(summary)
        }
    }

    data class DeathByLove(val dead: PlayerName) : ConfirmationStepPrompt<DeathByLove.Info>() {
        data class Info(val dead: PlayerName) : ConfirmationStepPrompt.Info, GameStepData.MarksPublicKilled {
            override val destination: Destination = Destination.All
            override val killed: Set<PlayerName> = setOf(dead)
        }

        override fun exists(game: Game): Boolean = true
        override fun getInfo(game: Game): Info = Info(dead)
    }

    data object CupidSetLovers : GameStepPrompt<CupidSetLovers.Data, CupidSetLovers.Error>() {
        data class Data(val player1: PlayerName, val player2: PlayerName) : GameStepData
        data class DeathByLove(val player: PlayerName) : GameStepData.MarksPublicKilled {
            override val killed: Set<PlayerName> = setOf(player)
        }

        sealed interface Error {
            data class PlayerIsDead(val player: PlayerName) : Error
            data object LoversAlreadySet : Error
            data class SamePlayerChosen(val player: PlayerName) : Error

        }

        override fun exists(game: Game): Boolean =
            game.hasAliveRole(Role.Cupid) && game.steps.filterIsInstance<Data>().none()

        override fun checkStepData(game: Game, data: Data): Error? {
            if (data.player1 == data.player2) {
                return Error.SamePlayerChosen(data.player1)
            }
            if (game.getLivingState(data.player1) is Game.LivingState.Dead) {
                return Error.PlayerIsDead(data.player1)
            }
            if (game.getLivingState(data.player2) is Game.LivingState.Dead) {
                return Error.PlayerIsDead(data.player2)
            }
            if (game.steps.filterIsInstance<Data>().any()) {
                return Error.LoversAlreadySet
            }
            return null
        }
    }

    data object GuardProtect : GameStepPromptChoosePlayer<GuardProtect.Data, GuardProtect.Error>() {
        data class Data(val player: PlayerName) : GameStepData
        sealed interface Error {
            data class PlayerWasProtectedLastTime(val player: PlayerName) : Error
            data class PlayerIsDead(val player: PlayerName) : Error
        }

        override fun createPrompt(player: PlayerName): Data = Data(player)
        override fun exists(game: Game): Boolean = game.hasAliveRole(Role.Guard)

        override fun checkStepData(game: Game, data: Data): Error? {
            if (game.getLivingState(data.player) is Game.LivingState.Dead) {
                return Error.PlayerIsDead(data.player)
            }
            val lastGuardProtect = game.steps.filterIsInstance<Data>().lastOrNull()
            if (lastGuardProtect != null && lastGuardProtect.player == data.player) {
                return Error.PlayerWasProtectedLastTime(data.player)
            }
            return null
        }
    }

    data object GuardResurrect : ConfirmationStepPrompt<GuardResurrect.Info>() {
        data class Info(val resurrected: PlayerName?) : ConfirmationStepPrompt.Info, GameStepData.MarksAlive {
            override val destination: Destination = Destination.None
            override val alive: Set<PlayerName> = resurrected?.let { setOf(it) }.orEmpty()
        }

        override fun exists(game: Game): Boolean =
            game.hasAliveRole(Role.Guard) && game.thisNight().filterIsInstance<GuardProtect.Data>().any()

        override fun getInfo(game: Game): Info {
            val guardProtects = game.thisNight().filterIsInstance<GuardProtect.Data>()
            val lastProtect = guardProtects.last()
            //todo parametre proteger de tout
            val possibleRessurect = game.thisNight().filterIsInstance<WerewolvesKill.Data>().firstOrNull {
                it.victim == lastProtect.player
            }?.victim
            return Info(possibleRessurect)
        }
    }

    data object WitchShow : ConfirmationStepPrompt<WitchShow.Info>() {
        data class Info(val killedByWolves: PlayerName?) : ConfirmationStepPrompt.Info {
            override val destination: Destination = Destination.Specific(Role.Witch)
        }

        override fun exists(game: Game): Boolean = game.hasAliveRole(Role.Witch)
        override fun getInfo(game: Game): Info {
            val killedByWolves: PlayerName? = game.thisNight().fold(null) { acc, step ->
                when (step) {
                    is WerewolvesKill.Data -> step.victim
                    is GameStepData.MarksAlive if step.alive.contains(acc) -> null
                    else -> acc
                }
            }
            return Info(killedByWolves)
        }
    }

    data object WitchStep : GameStepPrompt<WitchStep.Data, WitchStep.Error>() {
        sealed interface Data : GameStepData {
            data class Heal(val player: PlayerName) : Data, GameStepData.MarksAlive {
                override val alive = setOf(player)
            }

            data class Kill(val player: PlayerName) : Data, GameStepData.NightHiddenKill {
                override val hiddenKilled = setOf(player)
            }

            data object Skip : Data
        }

        sealed interface Error {
            data class HealedNotTargetedPlayer(val player: PlayerName) : Error
            data class HealedLivingPlayer(val player: PlayerName) : Error
            data object HealAlreadyUsed : Error
            data class KilledDeadPlayer(val player: PlayerName) : Error
            data object KillAlreadyUsed : Error
        }

        override fun exists(game: Game): Boolean = game.hasAliveRole(Role.Witch)

        override fun checkStepData(game: Game, data: Data): Error? {
            return when (data) {
                is Data.Heal -> {
                    val shownToWitch = game.getLast<WitchShow.Info>()?.killedByWolves
                    when {
                        shownToWitch == null || shownToWitch != data.player ->
                            Error.HealedNotTargetedPlayer(data.player)

                        game.getLivingState(data.player) is Game.LivingState.Alive ->
                            Error.HealedLivingPlayer(data.player)

                        game.steps.filterIsInstance<Data.Heal>().any<Data.Heal>() ->
                            Error.HealAlreadyUsed

                        else -> null
                    }
                }

                is Data.Kill -> when {
                    game.getLivingState(data.player) is Game.LivingState.Dead -> {
                        Error.KilledDeadPlayer(data.player)
                    }

                    else -> {
                        val alreadyKilled = game.steps.filterIsInstance<Data.Kill>().any()
                        if (alreadyKilled) {
                            Error.KillAlreadyUsed
                        } else null
                    }
                }

                is Data.Skip -> null
            }
        }

    }

    data object MayorElection : GameStepPromptChoosePlayer<MayorElection.Data, MayorElection.Errors>() {
        override fun exists(game: Game): Boolean = alreadyHasMayor(game) == null
        data class Data(val mayor: PlayerName) : GameStepData

        override fun createPrompt(player: PlayerName): Data = Data(player)
        sealed interface Errors {
            data class MayorAlreadyElected(val alreadyElected: PlayerName) : Errors
        }

        private fun alreadyHasMayor(game: Game): PlayerName? {
            val mayor = game.steps.asReversed().filterIsInstance<Data>().firstOrNull()?.mayor
            return mayor?.let { it.takeIf { game.getLivingState(mayor) is Game.LivingState.Alive } }
        }

        override fun checkStepData(game: Game, data: Data): Errors? {
            val alreadyElected = alreadyHasMayor(game)
            if (alreadyElected != null) {
                return Errors.MayorAlreadyElected(alreadyElected)
            }
            return null
        }
    }

    data object VillagersKillVote : GameStepPromptChoosePlayer<VillagersKillVote.Data, VillagersKillVote.Errors>() {
        data class Data(val victim: PlayerName) : GameStepData, GameStepData.MarksPublicKilled {
            override val killed = setOf(victim)
        }

        override fun exists(game: Game): Boolean = true

        override fun createPrompt(player: PlayerName): Data = Data(player)

        sealed interface Errors {
            data class VictimAlreadyDead(val alreadyDead: PlayerName) : Errors
        }

        override fun checkStepData(game: Game, data: Data): Errors? {
            if (game.getLivingState(data.victim) is Game.LivingState.Dead) {
                return Errors.VictimAlreadyDead(data.victim)
            }
            return null
        }

    }

    data object HunterKill : GameStepPromptChoosePlayer<HunterKill.Data, HunterKill.Errors>() {
        data class Data(val victim: PlayerName) : GameStepData, GameStepData.MarksPublicKilled {
            override val killed = setOf(victim)
        }

        override fun exists(game: Game): Boolean = game.hasAliveRole(Role.Hunter)

        override fun createPrompt(player: PlayerName): Data = Data(player)

        sealed interface Errors {
            data class VictimAlreadyDead(val alreadyDead: PlayerName) : Errors
        }

        override fun checkStepData(game: Game, data: Data): Errors? {
            if (game.getLivingState(data.victim) is Game.LivingState.Dead) {
                return Errors.VictimAlreadyDead(data.victim)
            }
            return null
        }

    }

    data object WerewolvesKill : GameStepPromptChoosePlayer<WerewolvesKill.Data, WerewolvesKill.Errors>() {
        data class Data(val victim: PlayerName) : GameStepData, GameStepData.NightHiddenKill {
            override val hiddenKilled = setOf(victim)
        }

        override fun exists(game: Game): Boolean = game.hasAliveRole<Role.CalledWithWolves>()

        override fun createPrompt(player: PlayerName): Data =
            Data(player)

        sealed interface Errors {
            data class VictimAlreadyDead(val alreadyDead: PlayerName) : Errors
        }

        override fun checkStepData(game: Game, data: Data): Errors? {
            if (game.getLivingState(data.victim) is Game.LivingState.Dead) {
                return Errors.VictimAlreadyDead(data.victim)
            }
            return null
        }
    }

    data object WhiteWolfKill : GameStepPromptChoosePlayer<WhiteWolfKill.Data, WhiteWolfKill.Errors>() {
        data class Data(val victim: PlayerName) : GameStepData, GameStepData.NightHiddenKill {
            override val hiddenKilled = setOf(victim)
        }

        override fun exists(game: Game): Boolean {
            if (!game.hasAliveRole(Role.WhiteWolf)) return false
            val nightCount = game.steps.count { it is NightBegin.Info }
            return nightCount % 2 == 0
        }

        override fun createPrompt(player: PlayerName): Data = Data(player)

        sealed interface Errors {
            data class VictimAlreadyDead(val alreadyDead: PlayerName) : Errors
        }

        override fun checkStepData(game: Game, data: Data): Errors? {
            if (game.getLivingState(data.victim) is Game.LivingState.Dead) {
                return Errors.VictimAlreadyDead(data.victim)
            }
            return null
        }
    }

    data object SeerSee : GameStepPromptChoosePlayer<SeerSee.Data, Nothing>() {
        data class Data(val player: PlayerName) : GameStepData

        override fun exists(game: Game): Boolean = game.hasAliveRole(Role.Seer)
        override fun createPrompt(player: PlayerName): Data = Data(player)
        override fun checkStepData(game: Game, data: Data): Nothing? = null
    }

    data object SeerShow : ConfirmationStepPrompt<SeerShow.Info>() {
        data class Info(val player: PlayerName, val role: Role) : ConfirmationStepPrompt.Info {
            override val destination: Destination = Destination.Specific(Role.Seer)
        }

        override fun exists(game: Game): Boolean = game.hasAliveRole(Role.Seer)
        override fun getInfo(game: Game): Info {
            val player = game.getLast<SeerSee.Data>()!!.player
            return Info(player, game.getRoles(player).first())
        }
    }
}

private fun Game.thisNight(): List<GameStepData> {
    return this.steps.asReversed().takeWhile { it !is GameStepPrompt.NightBegin.Info }.reversed()
}












