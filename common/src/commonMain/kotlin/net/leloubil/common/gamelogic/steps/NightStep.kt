package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.roles.BaseCall
import net.leloubil.common.gamelogic.roles.WerewolvesCall
import net.leloubil.common.gamelogic.roles.WitchCall
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass

val callOrder: List<Pair<KClass<out BaseCall>, (GameDefinition) -> BaseCall>> =
    listOf(
        Pair(WerewolvesCall::class, ::WerewolvesCall),
        Pair(WitchCall::class, ::WitchCall)
    )


class Night(gameDefinition: GameDefinition) : GameStep("Night", gameDefinition) {
    init {
        val nightEnd = finalState("Night End")
        val callsOrder = callOrder
            .filter { call -> gameDefinition.playerList.any { it.role.participatesIn.contains(call.first) } }
            .map { call -> call.second(gameDefinition) }
        //in reverse

        var lastCheck: State = nightEnd
        var lastCall: State = nightEnd
        callsOrder.reversed()
            .forEach { call ->
                val theCall = addState(call) {
                    transition<FinishedEvent> {
                        targetState = lastCheck
                    }
                }
                val theCheck = state(call.name + " Check") {
                    transition<FinishedEvent>("At least one participant") {
                        guard = {
                            gameDefinition.playerList.filter { it.alive }.any {
                                it.role.participatesIn.contains(call::class)
                            }
                        }
                        targetState = theCall
                    }

                    transition<FinishedEvent>("No participant") {
                        guard = {
                            gameDefinition.playerList.filter { it.alive }.none {
                                it.role.participatesIn.contains(call::class)
                            }
                        }
                        targetState = lastCheck
                    }
                }

                lastCall = theCall
                lastCheck = theCheck
            }

        initialState("Night Start")
        {
            transition<FinishedEvent> {
                targetState = lastCheck
            }
        }


    }
}
