package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.roles.BaseCall
import net.leloubil.common.gamelogic.roles.WerewolvesCall
import net.leloubil.common.gamelogic.roles.WitchCall
import ru.nsk.kstatemachine.*

val callsOrderBuilder : (GameDefinition) -> List<BaseCall> = {listOf(
    WerewolvesCall(it),
    WitchCall(it)
)}

class Night(gameDefinition: GameDefinition, gameEndState: State) : GameStep("Night", gameDefinition) {
    init {
        val nightEnd = finalState("Night End")
        val processKills = addState(ProcessKillsStep("Process Kills Night",gameDefinition))
        val checkWin = addState((CheckWinStep("Check Win Night",gameDefinition, gameEndState, nightEnd)))
        val callsOrder = callsOrderBuilder(gameDefinition)
        //in reverse

        var lastState : State = processKills
        callsOrder.reversed().forEach {
            addState(it){
                transition<FinishedEvent> {
                    targetState = lastState
                }
            }
            lastState = it
        }

        initialState("Night Start"){
            transition<FinishedEvent> {
                targetState = callsOrder.first()
            }
        }

        processKills{
            transition<FinishedEvent> {
                targetState = checkWin
            }
        }


    }
}
