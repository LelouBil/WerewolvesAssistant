package net.leloubil.werewolvesassistant.engine

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.optics.optics


sealed class GameStepPromptChoosePlayer<T : GameStepData, E> : GameStepPrompt<T, E>() {
    protected abstract fun createPrompt(player: PlayerName): T

    fun getValidPlayers(game: Game): Set<PlayerName> {
        return game.players.filter {
            this.checkStepData(game, this.createPrompt(it)) == null
        }.toSet()
    }
}

sealed interface Destination {
    data object All : Destination
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
}


sealed interface Role {
    sealed interface Team {
        sealed interface VillagersTeam : Team
        sealed interface WerewolvesTeam : Team

    }

    data object SimpleVillager : Role, Team.VillagersTeam

    data object Seer : Role, Team.VillagersTeam

    data object Hunter : Role, Team.VillagersTeam

    data object Werewolf : Role, Team.WerewolvesTeam

}

@optics
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

sealed class GameStepPrompt<T : GameStepData, E> {
    abstract fun exists(game: Game): Boolean

    abstract fun checkStepData(game: Game, data: T): E?

    context(_: Raise<E>)
    fun process(game: Game, data: T): Either<GameEnd, Game> {
        return game.applyPrompt(data, this)
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
            val summary = game.steps.asReversed().takeWhile { it !is NightBegin.Info }
                .filterIsInstance<GameStepData.NightHiddenKill>()
                .reversed().flatMap {
                    it.hiddenKilled.map { player -> player to game.getRoles(player) } //todo peut être afficher uniquement le premier role
                }
            return Info(summary)
        }
    }


    data object MayorElection : GameStepPromptChoosePlayer<MayorElection.Data, MayorElection.Errors>() {
        override fun exists(game: Game): Boolean = alreadyHasMayor(game) == null
        data class Data(val mayor: PlayerName) : GameStepData

        override fun createPrompt(player: PlayerName): Data = Data(player)
        sealed interface Errors {
            data class MayorAlreadyElected(val alreadyElected: PlayerName) : Errors
        }

        private fun alreadyHasMayor(game: Game): PlayerName? =
            game.steps.asReversed().filterIsInstance<Data>().firstOrNull()?.mayor

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
        data class Data(val victim: PlayerName) : GameStepData, GameStepData.MarksPublicKilled {
            override val killed = setOf(victim)
        }

        override fun exists(game: Game): Boolean = game.hasAliveRole(Role.Werewolf)

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












