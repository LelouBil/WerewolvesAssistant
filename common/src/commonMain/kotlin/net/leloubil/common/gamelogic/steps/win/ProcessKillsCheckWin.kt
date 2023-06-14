package net.leloubil.common.gamelogic.steps.win

import net.leloubil.common.gamelogic.MutableGameDefinition
import net.leloubil.common.gamelogic.QueueUndoEventHandler
import ru.nsk.kstatemachine.*


open class ProcessKillsCheckWin(
    name: String,
    private val gameDefinition: MutableGameDefinition,
    private val endGameStep: State,
) : DefaultState("Process Kills and Check Win $name") {

    inner class ProcessKillsStepPart: ProcessKillsStep("Process Kills $name", gameDefinition){
        inner class ConfirmKillsEvent : Event

        init{
            onEntry {
                machine.processEvent(QueueUndoEventHandler.FinishedUndoEvent)
            }
        }
    }
    inner class CheckWinStepPart: CheckWinStep("Check Win $name", gameDefinition, endGameStep, noWinner)

    private val noWinner = finalState("No Winner $name")

    private val processKills = addInitialState(ProcessKillsStepPart())

    private val checkWin = addState(CheckWinStepPart())

    init {

        processKills {
            transition<ProcessKillsStepPart.ConfirmKillsEvent>("Confirm Kills $name") {
                targetState = this@ProcessKillsCheckWin.checkWin
            }
        }
    }
}
