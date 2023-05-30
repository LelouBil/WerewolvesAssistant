
package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import ru.nsk.kstatemachine.FinishedEvent
import ru.nsk.kstatemachine.State
import ru.nsk.kstatemachine.invoke
import ru.nsk.kstatemachine.transition

class ProcessKillsCheckWin(
    name: String,
    gameDefinition: GameDefinition,
    endGameStep: State,
    continueStep: State
) : GameStep("Process Kills and Check Win $name", gameDefinition) {

    val processKills = addState(ProcessKillsStep("Process Kills $name ", gameDefinition))

    private val checkWin = addState(CheckWinStep("Check Win $name", gameDefinition, endGameStep, continueStep))

    init {
        processKills {
            transition<FinishedEvent> {
                targetState = this@ProcessKillsCheckWin.checkWin
            }
        }
    }
}
