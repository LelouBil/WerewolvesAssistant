@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package net.leloubil.common.gamelogic

import kotlinx.coroutines.CoroutineScope
import net.leloubil.common.gamelogic.roles.BaseRole
import net.leloubil.common.gamelogic.steps.Night
import net.leloubil.common.gamelogic.steps.day.Day
import ru.nsk.kstatemachine.*

class GameEndState() : DefaultFinalState("Game Ended")


class ShowRolesState(gameDefinition: GameDefinition) : DefaultState("Show roles") {
    val playerRoleList: MutableList<Pair<String, BaseRole>> = mutableListOf()

    inner class ConfirmRolesEvent : Event

    init {
        onEntry {
            playerRoleList.clear()
            playerRoleList.addAll(gameDefinition.playerList.map { it.name to it.role })
        }
    }
}


suspend fun createStateMachine(
    scope: CoroutineScope,
    rolesList: Set<BaseRole>,
    gameDefinition: GameDefinition
): StateMachine = createStateMachine(
    scope = scope,
    name = "Game State Machine",
    enableUndo = true,
    doNotThrowOnMultipleTransitionsMatch = false,
    start = false
) {
    //            logger = StateMachine.Logger { lazyMessage -> Napier.i(tag ="StateMachine") { lazyMessage() } }
    val showRolesState = addInitialState(ShowRolesState(gameDefinition))
    val gameEndState = addFinalState(GameEndState())
    val day = addState(Day(gameDefinition, gameEndState))
    val night = addState(Night(gameDefinition))

    showRolesState {
        transition<ShowRolesState.ConfirmRolesEvent>("Confirm roles") {
            targetState = night
        }
    }

    day {
        transition<FinishedEvent>("Day finished") {
            targetState = night
        }
    }
    night {
        transition<FinishedEvent>("Night finished") {
            targetState = day
        }
    }
}.apply {
    rolesList.distinctBy { it::class }.forEach {
        val overrideStateMachine = it.overrideStateMachine
        overrideStateMachine?.invoke(this)
    }
}


suspend fun createGameDefinition(
    scope: CoroutineScope,
    playerNamesList: List<String>,
    rolesList: Set<BaseRole>
): GameDefinition {

    if (rolesList.size < playerNamesList.size) {
        throw IllegalArgumentException("Not enough players for the given roles")
    }

    val playerList: List<Player> =
        playerNamesList.shuffled().zip(rolesList.shuffled()).map { Player(it.first, it.second) }
    val gameDefinition = GameDefinition(playerList)
    gameDefinition.stateMachine = createStateMachine(scope, rolesList, gameDefinition)
    return gameDefinition
}

