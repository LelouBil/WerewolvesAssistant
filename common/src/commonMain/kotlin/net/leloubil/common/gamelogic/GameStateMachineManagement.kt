@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package net.leloubil.common.gamelogic

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import net.leloubil.common.gamelogic.roles.BaseRole
import net.leloubil.common.gamelogic.steps.Night
import net.leloubil.common.gamelogic.steps.day.Day
import ru.nsk.kstatemachine.*

class GameEndState() : DefaultFinalState("Game Ended")


class ShowRolesState(gameDefinition: MutableGameDefinition) : ActionableDefaultStep("Show roles", gameDefinition) {
    var playerRoleList: List<Pair<String, BaseRole>> = listOf()
        private set

    inner class ConfirmRolesEvent : Event

    init {
        onEntry {
            machine.processEvent(QueueUndoEventHandler.FinishedUndoEvent)
        }
        action {
            ::playerRoleList undoAssign gameDefinition.playerList.map { it.name to it.role }
        }
    }
}


class QueueUndoEventHandler(private val machine: StateMachine,private val gameDef: MutableGameDefinition) : QueuePendingEventHandler {
    public object FinishedUndoEvent : Event

    private val queue = ArrayDeque<EventAndArgument<*>>()



    override suspend fun checkEmpty() = check(queue.isEmpty()) { "Event queue is not empty, internal error" }

    override suspend fun onPendingEvent(eventAndArgument: EventAndArgument<*>) {

        if(gameDef.isUndoing){
            if(eventAndArgument.event is FinishedUndoEvent){
                machine.log { "Undo finished" }
                gameDef.isUndoing = false
            }
            return
        }
        else if (eventAndArgument.event is UndoEvent) {
            machine.log {
                "Undo starting"
            }
            gameDef.isUndoing = true
            return
        }
        machine.log {
            "$machine queued event ${eventAndArgument.event::class.simpleName} with argument ${eventAndArgument.argument}"
        }
        queue.add(eventAndArgument)
    }

    override suspend fun nextEventAndArgument() =
        if (gameDef.isUndoing) EventAndArgument(UndoEvent, null) else queue.removeFirstOrNull()

    override suspend fun clear() = queue.clear()
}

suspend fun createStateMachine(
    scope: CoroutineScope,
    rolesList: Set<BaseRole>,
    gameDefinition: MutableGameDefinition
): StateMachine = createStateMachine(
    scope = scope,
    name = "Game State Machine",
    enableUndo = true,
    doNotThrowOnMultipleTransitionsMatch = false,
    start = false
) {
    logger = StateMachine.Logger { lazyMessage -> Napier.i(tag = "StateMachine") { lazyMessage() } }
    pendingEventHandler = QueueUndoEventHandler(this, gameDefinition)
    addListener(object : StateMachine.Listener {
        override suspend fun onStateExit(state: IState, transitionParams: TransitionParams<*>) {
            if (gameDefinition.isUndoing) {
                when (state) {
                    is ActionableDefaultStep -> {
                        state.doUndo()
                    }

                    is ActionableDataStep<*> -> {
                        state.doUndo()
                    }
                }
            }
        }
    })
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
): ReadOnlyGameDefinition {

    if (rolesList.size < playerNamesList.size) {
        throw IllegalArgumentException("Not enough players for the given roles")
    }

    val playerList: List<Player> =
        playerNamesList.shuffled().zip(rolesList.shuffled()).map { Player(it.first, it.second) }
    val gameDefinition = MutableGameDefinition(playerList)
    gameDefinition.stateMachine = createStateMachine(scope, rolesList, gameDefinition)
    return gameDefinition
}

