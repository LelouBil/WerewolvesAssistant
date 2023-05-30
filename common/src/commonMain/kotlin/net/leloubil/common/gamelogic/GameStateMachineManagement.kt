@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package net.leloubil.common.gamelogic

import kotlinx.coroutines.CoroutineScope
import net.leloubil.common.gamelogic.roles.BaseRole
import net.leloubil.common.gamelogic.steps.Day
import net.leloubil.common.gamelogic.steps.Night
import org.lighthousegames.logging.logging
import ru.nsk.kstatemachine.*

class GameStateMachineHolder(scope: CoroutineScope,
    gameDefinition: GameDefinition){
    companion object {
        val smLogger = logging("StateMachine")
    }
    val stateMachine : StateMachine
    lateinit var gameStartState : State
    lateinit var gameEndState : State
    lateinit var day : Day
    lateinit var night : Night
    init{
        stateMachine = createStateMachineBlocking(
            scope = scope,
            name = "Game State Machine",
            enableUndo = true,
            doNotThrowOnMultipleTransitionsMatch = false,
            start = false
        ) {
            logger = StateMachine.Logger { lazyMessage -> smLogger.info { lazyMessage() } }
            gameStartState = initialState("Game not yet started")
            gameEndState = finalState("Game finished")
            day = addState(Day(gameDefinition,gameEndState))
            night = addState(Night(gameDefinition))

            gameStartState {
                transition<FinishedEvent>("Game started") {
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
        }
    }
}

public fun createGameStateMachineBuilder(
    rolesList: Set<BaseRole>,
    gameDefinition: GameDefinition
): suspend (CoroutineScope) -> GameStateMachineHolder = { scope: CoroutineScope ->
    GameStateMachineHolder(scope, gameDefinition).apply {
        rolesList.distinctBy { it::class }.forEach {
            val overrideStateMachine = it.overrideStateMachine
            overrideStateMachine?.invoke(this)
        }
    }
}


public fun createGameDefinition(
    playerNamesList: List<String>,
    rolesList: Set<BaseRole>
): GameDefinition {

    assert(playerNamesList.size == rolesList.size)
    assert(playerNamesList.size >= 4)

    val playerList: List<Player> =playerNamesList.shuffled().zip(rolesList.shuffled()).map { Player(it.first,it.second)}
    val gameDefinition = GameDefinition(playerList)
    val stateMachineBuilder = createGameStateMachineBuilder(rolesList, gameDefinition)
    gameDefinition.buildStateMachine = stateMachineBuilder
    return gameDefinition
}

