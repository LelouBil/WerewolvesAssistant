package net.leloubil.werewolvesassistant.engine

import arrow.core.Either
import arrow.core.left
import arrow.core.right

val scheduleOrder = listOf(
    GameStepPrompt.NightBegin,
    GameStepPrompt.SeerSee,
    GameStepPrompt.SeerShow,
    GameStepPrompt.WerewolvesKill,
    GameStepPrompt.NightEnd,
    GameStepPrompt.MayorElection,
    GameStepPrompt.VillagersKillVote,
)

sealed interface ScheduleErrors {
    data object NoValidStepError : ScheduleErrors
}

sealed interface GameEnd {
    val winningPlayers: Set<PlayerName>

    data class VillagersWon(private val villagers: Set<PlayerName>) : GameEnd {
        override val winningPlayers: Set<PlayerName> = villagers
    }

    data class WerewolvesWon(private val werewolves: Set<PlayerName>) : GameEnd {
        override val winningPlayers: Set<PlayerName> = werewolves
    }

    data class ScheduleError(val error: ScheduleErrors) : GameEnd {
        override val winningPlayers: Set<PlayerName> = emptySet()
    }

}




