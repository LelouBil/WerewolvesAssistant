package net.leloubil.werewolvesassistant.engine

import arrow.core.Either
import arrow.core.left
import arrow.core.right

val scheduleOrder = listOf(
    GameStepPrompt.NightBeginPrompt,
    GameStepPrompt.WerewolvesKillPrompt,
    GameStepPrompt.NightEndPrompt,
    GameStepPrompt.VillagersVotePrompt,
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

fun scheduleNext(game: Game, step: GameStepPrompt<*, *>): Either<GameEnd, GameStepPrompt<*, *>> {
    //todo game end


    val lastStepIdx = scheduleOrder.indexOf(step)
    val steps = (scheduleOrder.asSequence().drop(lastStepIdx) + scheduleOrder).firstOrNull { it.exists(game) }
    return steps?.right() ?: GameEnd.ScheduleError(ScheduleErrors.NoValidStepError).left()
}

fun nextStep(game: Game, schedStep: GameStepPrompt<*, *>, gameStepData: GameStepData): Game {
    return when (val nextPrompt = scheduleNext(game, schedStep)) {
        is Either.Left<GameEnd> -> {
            game.copy(
                steps = game.steps + gameStepData,
                end = nextPrompt.value
            )
        }

        is Either.Right<GameStepPrompt<*, *>> -> {
            game.copy(
                steps = game.steps + gameStepData,
                nextPrompt = nextPrompt.value
            )
        }
    }
}
