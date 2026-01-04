package net.leloubil.werewolvesassistant.engine

import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.optics.optics
import kotlin.collections.any
import kotlin.collections.component1
import kotlin.collections.component2

sealed interface Role {
    data object Villager : Role

    data object Werewolf : Role
}


@optics
sealed interface GameStepData {
    companion object;
    interface MarksKilled : GameStepData {
        val killed: Set<PlayerName>
    }

    interface MarksAlive : GameStepData {
        val alive: Set<PlayerName>
    }

    data object ConfirmationData : GameStepData

    data class VillagersKillVoteData(
        val victim: PlayerName
    ) : GameStepData

    data class WerewolvesKillStepData(
        val victim: PlayerName
    ) : GameStepData, MarksKilled {
        override val killed = setOf(victim)
    }
}

sealed class GameStepPrompt<T : GameStepData, E> {
    abstract fun exists(game: Game): Boolean

    protected abstract fun checkStepData(game: Game, data: T): E?

    sealed class GameStepPromptChoosePlayer<T : GameStepData, E> : GameStepPrompt<T, E>() {
        protected abstract fun createPrompt(player: PlayerName): T

        fun getValidPlayers(game: Game): Set<PlayerName> {
            return game.players.keys.filter {
                this.checkStepData(game, this.createPrompt(it)) == null
            }.toSet()
        }
    }

    sealed class ConfirmationStepPrompt : GameStepPrompt<GameStepData.ConfirmationData, Nothing>() {
        override fun checkStepData(game: Game, data: GameStepData.ConfirmationData): Nothing? = null
    }


    context(_: Raise<E>)
    fun process(game: Game, stepData: T): Game {
        val possibleError = checkStepData(game, stepData)
        if (possibleError != null) {
            raise(possibleError)
        }
        return nextStep(game, this, stepData)
    }

    data object WerewolvesKillPrompt :
        GameStepPromptChoosePlayer<GameStepData.WerewolvesKillStepData,
                WerewolvesKillPrompt.WerewolvesKillPromptErrors>() {
        override fun exists(game: Game): Boolean =
            game.players.any { (p, data) ->
                game.getLivingState(p) is Game.LivingState.Alive
                        && data.role is Role.Werewolf
            }

        sealed interface WerewolvesKillPromptErrors {
            data class VictimAlreadyDead(val alreadyDead: PlayerName) : WerewolvesKillPromptErrors
        }

        override fun createPrompt(player: PlayerName): GameStepData.WerewolvesKillStepData =
            GameStepData.WerewolvesKillStepData(player)

        override fun checkStepData(game: Game, data: GameStepData.WerewolvesKillStepData): WerewolvesKillPromptErrors? {
            if (game.getLivingState(data.victim) is Game.LivingState.Dead) {
                return WerewolvesKillPromptErrors.VictimAlreadyDead(data.victim)
            }
            return null
        }
    }

    data object NightBeginPrompt : ConfirmationStepPrompt() {
        override fun exists(game: Game): Boolean = true

    }

    data object NightEndPrompt : ConfirmationStepPrompt() {
        override fun exists(game: Game): Boolean = true
    }

    data object VillagersVotePrompt :
        GameStepPromptChoosePlayer<GameStepData.VillagersKillVoteData, VillagersVotePrompt.VillagersKillVotePromptErrors>() {
        override fun exists(game: Game): Boolean = true

        override fun checkStepData(
            game: Game,
            data: GameStepData.VillagersKillVoteData
        ): VillagersKillVotePromptErrors? {
            if (game.getLivingState(data.victim) is Game.LivingState.Dead) {
                return VillagersKillVotePromptErrors.VictimAlreadyDead(data.victim)
            }
            return null
        }

        override fun createPrompt(player: PlayerName): GameStepData.VillagersKillVoteData =
            GameStepData.VillagersKillVoteData(player)

        sealed interface VillagersKillVotePromptErrors {
            data class VictimAlreadyDead(val alreadyDead: PlayerName) : VillagersKillVotePromptErrors
        }
    }
}

