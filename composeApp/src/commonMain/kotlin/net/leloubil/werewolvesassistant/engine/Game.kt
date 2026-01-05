package net.leloubil.werewolvesassistant.engine

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.right
import arrow.optics.optics

@optics
data class Game(
    val players: List<PlayerName>,
    val steps: List<GameStepData>,
    private val nextPrompts: List<GameStepPrompt<*, *>>,
) {
    companion object;
    sealed interface LivingState {
        data class Alive(val cause: GameStepData.MarksAlive?) : LivingState
        data class Dead(val cause: GameStepData.MarksPublicKilled) : LivingState
    }

    context(_: Raise<E>)
    fun <P : GameStepPrompt<T, E>, T : GameStepData, E> applyPrompt(data: T, prompt: P): Either<GameEnd, Game> {
        val possibleError = prompt.checkStepData(this, data)
        if (possibleError != null) {
            raise(possibleError)
        }
        return copy(steps = steps + data).scheduleNext()
    }

    private fun scheduleNext(): Either<GameEnd, Game> {
        val game = this
        if (game.steps.last().checkGameEnd) {
            val livingPlayers = game.players.filter { game.getLivingState(it) is Game.LivingState.Alive }
            inline fun <reified T : Role.Team> List<List<Role>>.allOfTeam(): Boolean {
                return all { it.filterIsInstance<T>().any() }
            }
            if (livingPlayers.map { game.getRoles(it) }.allOfTeam<Role.Team.VillagersTeam>()) {
                return GameEnd.VillagersWon(livingPlayers.toSet()).left()
            } else if (livingPlayers.map { game.getRoles(it) }.allOfTeam<Role.Team.WerewolvesTeam>()) {
                return GameEnd.WerewolvesWon(livingPlayers.toSet()).left()
            }
        }

        val last = game.steps.last()
        if (last is GameStepData.MarksPublicKilled) {
            val killedPlayers = last.killed
            if (killedPlayers.map { game.getRoles(it) }.any { it.contains(Role.Hunter) }) {
                return game.copy(
                    nextPrompts = game.nextPrompts + listOf(GameStepPrompt.HunterKill)
                ).right()
            }
        }

        if (game.nextPrompts.isEmpty()) {
            return game.copy(nextPrompts = scheduleOrder.filter { it.exists(game) }).right()
        }
        return game.right()
    }

}

inline fun <reified D : GameStepData> Game.getLast(): D? {
    return steps.asReversed().filterIsInstance<D>().firstOrNull()
}

inline fun <reified T : Role> Game.hasAliveRole(role: T): Boolean {
    return players.any { p -> getRoles(p).contains(role) && getLivingState(p) is Game.LivingState.Alive }
}

fun Game.getRoles(player: PlayerName): List<Role> =
    steps.asReversed().filterIsInstance<GameStepData.SetsRole>().firstNotNullOf {
        it.assignments[player]
    }

fun Game.getLivingState(player: PlayerName): Game.LivingState {
    return steps.asReversed().firstNotNullOfOrNull {
        when (it) {
            is GameStepData.MarksPublicKilled if it.killed.contains(player) -> {
                Game.LivingState.Dead(it)
            }

            is GameStepData.MarksAlive if it.alive.contains(player) -> {
                Game.LivingState.Alive(it)
            }

            else -> null
        }
    } ?: Game.LivingState.Alive(null)
}


