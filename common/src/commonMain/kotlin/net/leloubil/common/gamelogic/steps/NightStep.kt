package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.MutableGameDefinition
import net.leloubil.common.gamelogic.QueueUndoEventHandler
import net.leloubil.common.gamelogic.roles.BaseCall
import net.leloubil.common.gamelogic.roles.WerewolvesCall
import net.leloubil.common.gamelogic.roles.WitchCall
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass

val callOrder: List<Pair<KClass<out BaseCall>, (MutableGameDefinition) -> BaseCall>> =
    listOf(
        Pair(WerewolvesCall::class, ::WerewolvesCall),
        Pair(WitchCall::class, ::WitchCall)
    )
class CheckState(name: String, gameDefinition: MutableGameDefinition) : SelfContinueDefaultStep(name, gameDefinition)
class ConfirmNightStartEvent : Event
class Night(gameDefinition: MutableGameDefinition) : DefaultState("Night") {
    init {
        val nightEnd = finalState("Night End")
        val callsOrder = callOrder
            .filter { call -> gameDefinition.playerList.any { it.role.participatesIn.contains(call.first) } }
            .map { call -> call.second(gameDefinition) }
        //in reverse

        var lastCheck: State = nightEnd
        callsOrder.reversed()
            .forEach { call ->
                val theCall = addState(call) {
                    transition<FinishedEvent> {
                        targetState = lastCheck
                    }
                }
                val theCheck = addState(CheckState(call.name + " Check",gameDefinition)) {
                    selfContinuation("At least one participant") {
                        guard = {
                            gameDefinition.playerList.filter { it.alive }.any {
                                it.role.participatesIn.contains(call::class)
                            }
                        }
                        targetState = theCall
                    }
                    selfContinuation("No participant") {
                        guard = {
                            gameDefinition.playerList.filter { it.alive }.none {
                                it.role.participatesIn.contains(call::class)
                            }
                        }
                        targetState = lastCheck
                    }
                }
                lastCheck = theCheck
            }

        addInitialState(NightStartState())
        {
            transition<ConfirmNightStartEvent> {
                targetState = lastCheck
            }
        }


    }
}

class NightStartState : DefaultState("Night Start") {
    init{
        onEntry {
            machine.processEvent(QueueUndoEventHandler.FinishedUndoEvent)
        }
    }
}
