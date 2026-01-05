package net.leloubil.werewolvesassistant.engine

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.Raise
import arrow.core.raise.context.raise
import arrow.core.right
import kotlinx.serialization.Serializable

private val scheduleOrder = listOf(
    GameStepPrompt.NightBegin,
    GameStepPrompt.CupidSetLovers,
    GameStepPrompt.GuardProtect,
    GameStepPrompt.SeerSee,
    GameStepPrompt.SeerShow,
    GameStepPrompt.WerewolvesKill,
    GameStepPrompt.GuardResurrect,
    GameStepPrompt.WitchShow,
    GameStepPrompt.WitchStep,
    GameStepPrompt.NightEnd,
    GameStepPrompt.MayorElection,
    GameStepPrompt.VillagersKillVote,
)

typealias RolesList = List<Pair<PlayerName, Role>>

@Serializable
data class Game private constructor(
    val players: List<PlayerName>,
    val steps: List<GameStepData>,
    private val nextPrompts: List<GameStepPrompt<*, *>>,
) {
    val nextPrompt: GameStepPrompt<*, *>? = nextPrompts.firstOrNull()

    sealed interface LivingState {
        data class Alive(val cause: GameStepData.MarksAlive?) : LivingState
        sealed class Dead: LivingState{
            abstract val cause: GameStepData
        }
        data class PublicDead(override val cause: GameStepData.MarksPublicKilled) : Dead()
        data class NightHiddenDead(override val cause: GameStepData.NightHiddenKill) : Dead()

    }

    data class InitialRoles(override val assignments: Map<PlayerName, List<Role>>) : GameStepData.SetsRole

    companion object {
        operator fun invoke(players: List<Pair<PlayerName, Role>>): Game? {
            val g = Game(
                players = players.map { it.first },
                steps = listOf(InitialRoles(players.associate { it.first to listOf(it.second) })),
                nextPrompts = emptyList()
            )
            return g.scheduleNext().getOrNull()
        }
    }


    context(_: Raise<E>)
    fun <P : GameStepPrompt<T, E>, T : GameStepData, E> removeLastPromptAndApply(data: T, prompt: P): Either<GameEnd, Game> {
        val next = nextPrompt
        if (next != prompt) {
            //todo better error
            throw IllegalStateException("Trying to apply data that is not the last step")
        }
        val possibleError = prompt.checkStepData(this, data)
        if (possibleError != null) {
            raise(possibleError)
        }
        return copy(steps = steps + data, nextPrompts = nextPrompts.drop(1)).scheduleNext()
    }

    private inline fun <reified T : Role.Team> List<List<Role>>.allOfTeam(): Boolean {
        return all { it.filterIsInstance<T>().any() }
    }

    private fun scheduleNext(): Either<GameEnd, Game> {
        val game = this

        val last = game.steps.last()
        if (last is GameStepData.MarksPublicKilled) {
            val killedPlayers = last.killed
            if (killedPlayers.map { game.getRoles(it) }.any { it.contains(Role.Hunter) }) {
                return game.copy(
                    nextPrompts = listOf(GameStepPrompt.HunterKill) + game.nextPrompts
                ).right()
            }
            val lovers = game.steps.filterIsInstance<GameStepPrompt.CupidSetLovers.Data>().firstOrNull()
            if (lovers != null && (killedPlayers.contains(lovers.player1) || killedPlayers.contains(lovers.player2))) {
                if (!killedPlayers.containsAll(setOf(lovers.player1, lovers.player2))) {
                    val other = if (killedPlayers.contains(lovers.player1)) lovers.player2 else lovers.player1
                    return game.copy(
                        nextPrompts = listOf(GameStepPrompt.DeathByLove(other)) + game.nextPrompts
                    ).right()
                }
            }
        }

        if (game.steps.last().checkGameEnd) {
            val livingPlayers = game.players.filter { game.getLivingState(it) is LivingState.Alive }

            val lovers = game.steps.filterIsInstance<GameStepPrompt.CupidSetLovers.Data>().firstOrNull()
            if (lovers != null) {
                if (livingPlayers.toSet() == setOf(lovers.player1, lovers.player2)) {
                    return GameEnd.LoversWon(lovers.player1, lovers.player2).left()
                }
            }

            if (livingPlayers.map { game.getRoles(it) }.allOfTeam<Role.Team.VillagersTeam>()) {
                return GameEnd.VillagersWon(livingPlayers.toSet()).left()
            } else if (livingPlayers.map { game.getRoles(it) }.allOfTeam<Role.Team.WerewolvesTeam>()) {
                return GameEnd.WerewolvesWon(livingPlayers.toSet()).left()
            }
        }

        if (game.nextPrompts.isEmpty()) {
            return game.copy(nextPrompts = scheduleOrder.filter { it.exists(game) }).right()
        }
        return game.right()
    }

}


sealed interface GameEnd {
    val winningPlayers: Set<PlayerName>

    data class VillagersWon(private val villagers: Set<PlayerName>) : GameEnd {
        override val winningPlayers: Set<PlayerName> = villagers
    }

    data class WerewolvesWon(private val werewolves: Set<PlayerName>) : GameEnd {
        override val winningPlayers: Set<PlayerName> = werewolves
    }

    class LoversWon(lover1: PlayerName, lover2: PlayerName) : GameEnd {
        override val winningPlayers: Set<PlayerName> = setOf(lover1, lover2)
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
                Game.LivingState.PublicDead(it)
            }
            is GameStepData.NightHiddenKill if it.hiddenKilled.contains(player) -> {
                Game.LivingState.NightHiddenDead(it)
            }

            is GameStepData.MarksAlive if it.alive.contains(player) -> {
                Game.LivingState.Alive(it)
            }

            else -> null
        }
    } ?: Game.LivingState.Alive(null)
}


