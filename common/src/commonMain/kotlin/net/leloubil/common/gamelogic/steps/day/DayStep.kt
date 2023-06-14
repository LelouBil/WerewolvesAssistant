package net.leloubil.common.gamelogic.steps.day

import net.leloubil.common.gamelogic.ActionableDefaultStep
import net.leloubil.common.gamelogic.MutableGameDefinition
import net.leloubil.common.gamelogic.QueueUndoEventHandler
import net.leloubil.common.gamelogic.steps.win.ProcessKillsCheckWin
import ru.nsk.kstatemachine.*


class Day(private val gameDefinition: MutableGameDefinition, private val gameEndState: State) : DefaultState("Day") {
    inner class StartState : ActionableDefaultStep("Day start", gameDefinition) {
        inner class ConfirmEvent : Event

        init {
            onEntry {
                machine.processEvent(QueueUndoEventHandler.FinishedUndoEvent)
            }
            action { this@Day.gameDefinition::dayNumber undoAssign this@Day.gameDefinition.dayNumber + 1 }
        }
    }

    inner class TryChangeMayorStartOfDay : TryChangeMayor("Start of Day", gameDefinition)
    inner class TryChangeMayorAfterVote : TryChangeMayor("After vote", gameDefinition)
    inner class ProcessDayKills : ProcessKillsCheckWin("Day", gameDefinition, gameEndState)
    inner class ProcessNightKills : ProcessKillsCheckWin("Night", gameDefinition, gameEndState)

    init {
        val dayStart = addInitialState(StartState())
        val villagerVoteStep = addState(VillagerVoteStep(gameDefinition))
        val tryChangeMayorStartOfDay = addState(TryChangeMayorStartOfDay())
        val processDayKills = addState(ProcessDayKills())
        val processNightKills = addState(ProcessNightKills())
        val tryChangeMayorAfterVote = addState(TryChangeMayorAfterVote())
        val dayEnd = finalState("Day End")

        dayStart {
            transition<Day.StartState.ConfirmEvent> {
                targetState = processNightKills
            }
        }

        processNightKills{
            transition<FinishedEvent>{
                targetState = tryChangeMayorStartOfDay
            }
        }

        tryChangeMayorStartOfDay {
            transition<FinishedEvent> {
                targetState = villagerVoteStep
            }
        }

        villagerVoteStep{
            transition<FinishedEvent>{
                targetState = processDayKills
            }
        }

        processDayKills{
            transition<FinishedEvent>{
                targetState = tryChangeMayorAfterVote
            }
        }

        tryChangeMayorAfterVote {
            transition<FinishedEvent> {
                targetState = dayEnd
            }
        }
    }
}
