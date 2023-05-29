@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package net.leloubil.common.gamelogic

import kotlinx.coroutines.CoroutineScope
import net.leloubil.common.gamelogic.roles.BaseRole
import net.leloubil.common.gamelogic.steps.Day
import net.leloubil.common.gamelogic.steps.Night
import net.leloubil.common.gamelogic.util.BiMap
import net.leloubil.common.gamelogic.util.HashBiMap
import org.lighthousegames.logging.logging
import ru.nsk.kstatemachine.*


public fun createGameStateMachineBuilder(
    rolesList: Set<BaseRole>,
    gameDefinition: GameDefinition
): suspend (CoroutineScope) -> StateMachine = { scope: CoroutineScope ->
    val smLogger = logging("StateMachine")
    createStateMachine(
        scope = scope,
        name = "Game State Machine",
        enableUndo = true,
        doNotThrowOnMultipleTransitionsMatch = false,
        start = false
    ) {
        logger = StateMachine.Logger { lazyMessage -> smLogger.info { lazyMessage() } }
        val gameStartState = initialState("Game not yet started")
        val gameEndState = finalState("Game finished")
        val day = addState(Day(gameDefinition,gameEndState))
        val night = addState(Night(gameDefinition,gameEndState))

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


public fun createGameDefinition(
    playerList: List<Player>,
    rolesList: Set<BaseRole>
): GameDefinition {

    assert(playerList.size == rolesList.size)
    assert(playerList.size >= 4)

    val rolesMapping: BiMap<Player, BaseRole> =
        HashBiMap.create(playerList.shuffled().zip(rolesList.shuffled()).toMap())
    val gameDefinition = GameDefinition(rolesMapping)
    val stateMachineBuilder = createGameStateMachineBuilder(rolesList, gameDefinition)
    gameDefinition.buildStateMachine = stateMachineBuilder
    return gameDefinition
}

