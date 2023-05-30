package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.roles.BaseCall
import net.leloubil.common.gamelogic.roles.WerewolvesCall
import net.leloubil.common.gamelogic.roles.WitchCall
import net.leloubil.common.gamelogic.util.StateEditor
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass

val callsOrderBuilder: (GameDefinition) -> List<BaseCall> = {
    listOf(
        WerewolvesCall(it),
        WitchCall(it)
    )
}

class Night(gameDefinition: GameDefinition) : GameStep("Night", gameDefinition) {
    init {
        val nightEnd = finalState("Night End")
        val callsOrder = callsOrderBuilder(gameDefinition)
        //in reverse

        var lastCheck: State = nightEnd
        var lastCall: State = nightEnd
        callsOrder.reversed().forEach { call ->
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
