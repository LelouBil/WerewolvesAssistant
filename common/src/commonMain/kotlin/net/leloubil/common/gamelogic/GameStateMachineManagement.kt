@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package net.leloubil.common.gamelogic

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import net.leloubil.common.gamelogic.roles.BaseRole
import net.leloubil.common.gamelogic.steps.Day
import net.leloubil.common.gamelogic.steps.GameStep
import net.leloubil.common.gamelogic.steps.Night
import ru.nsk.kstatemachine.*

class GameEndState() : DefaultFinalState("Game Ended")

class GameStateMachineHolder(
    scope: CoroutineScope,
    gameDefinition: GameDefinition,
) {
    val whenBuildFinished: MutableList<() -> Unit> = mutableListOf()
    lateinit var stateMachine: StateMachine
    lateinit var showRolesState: ShowRolesState
    lateinit var gameEndState: State
    lateinit var day: Day
    lateinit var night: Night
}

class ShowRolesState(gameDefinition: GameDefinition) : GameStep("Show roles",gameDefinition){
    lateinit var playerRoleList : List<Pair<String, BaseRole>>

    init{
        onEntry {
            playerRoleList = gameDefinition.playerList.map { it.name to it.role }
        }
    }
}
class ConfirmRolesEvent : Event

suspend fun addStateMachineHolder(
    scope: CoroutineScope,
    rolesList: Set<BaseRole>,
    gameDefinition: GameDefinition
): GameStateMachineHolder =
    GameStateMachineHolder(scope, gameDefinition).apply {
        gameDefinition.stateMachineHolder = this
        stateMachine = createStateMachine(
            scope = scope,
            name = "Game State Machine",
            enableUndo = true,
            doNotThrowOnMultipleTransitionsMatch = false,
            start = false
        ) {
            logger = StateMachine.Logger { lazyMessage -> Napier.i { lazyMessage() } }
            showRolesState = addInitialState(ShowRolesState(gameDefinition))
            gameEndState = addFinalState(GameEndState())
            day = addState(Day(gameDefinition, gameEndState))
            night = addState(Night(gameDefinition))

            showRolesState{
                transition<ConfirmRolesEvent>("Confirm roles"){
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
        rolesList.distinctBy { it::class }.forEach {
            val overrideStateMachine = it.overrideStateMachine
            overrideStateMachine?.invoke(this)
        }
        whenBuildFinished.forEach { it() }

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
    addStateMachineHolder(scope,rolesList, gameDefinition)
    return gameDefinition
}

