package net.leloubil.common.gamelogic.steps.win

import net.leloubil.common.gamelogic.GameDefinition
import ru.nsk.kstatemachine.*

class ConfirmKillsEvent : Event

open class ProcessKillsCheckWin(
    name: String,
    private val gameDefinition: GameDefinition,
    private val endGameStep: State,
) : DefaultState("Process Kills and Check Win $name") {

    inner class ProcessKillsStepPart: ProcessKillsStep("Process Kills $name", gameDefinition)
    inner class CheckWinStepPart: CheckWinStep("Check Win $name", gameDefinition, endGameStep, noWinner)

    private val noWinner = finalState("No Winner $name")

    private val processKills = addInitialState(ProcessKillsStepPart())

    private val checkWin = addState(CheckWinStepPart())

    init {
        processKills {
            transition<ConfirmKillsEvent>("Confirm Kills $name") {
                targetState = this@ProcessKillsCheckWin.checkWin
            }
        }
    }
}
