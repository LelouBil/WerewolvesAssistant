
package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import ru.nsk.kstatemachine.*

class ProcessKillsCheckWin(
    name: String,
    gameDefinition: GameDefinition,
    endGameStep: State,
    continueStep: State
) : GameStep("Process Kills and Check Win $name", gameDefinition) {

    val processKills = addInitialState(ProcessKillsStep("Process Kills $name ", gameDefinition))

    private val checkWin = addState(CheckWinStep("Check Win $name", gameDefinition, endGameStep, continueStep))

    init {
        processKills {
            selfContinuation {
                targetState = this@ProcessKillsCheckWin.checkWin
            }
        }
    }
}
