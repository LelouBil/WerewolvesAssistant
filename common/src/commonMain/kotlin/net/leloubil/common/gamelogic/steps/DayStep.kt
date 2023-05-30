package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.util.StateEditor
import ru.nsk.kstatemachine.*

class VillagerVoteKillEvent(override val data: Player) : DataEvent<Player>

class RevealKillsConfirmedEvent : Event
class VillagerVoteKill : PendingKill()
class Day(gameDefinition: GameDefinition, gameEndState: State) : GameStep("Day", gameDefinition) {

    val processKillsCheckWinEditor: StateEditor<ProcessKillsCheckWin> = StateEditor()
    val mayorChangeEditor: StateEditor<ChangeMayor> = StateEditor()
    val dayStart = initialState("Day Start")
    val mayorChangeStartOfDay = addState(ChangeMayor("Start of Day", gameDefinition).apply { this@Day.mayorChangeEditor})
    val processNightKills = addState(ProcessKillsCheckWin("Night", gameDefinition, gameEndState, mayorChangeStartOfDay).apply { this@Day.processKillsCheckWinEditor })
    val beforeVillagerKillVote = state("Villager Vote Start")
    val afterVillagerKillVote = dataState<Player>("Villager Killed")
    val ensureMayor = state("Ensure Mayor")
    val processDayKills = addState(ProcessKillsCheckWin("Day", gameDefinition, gameEndState, ensureMayor).apply { this@Day.processKillsCheckWinEditor })
    val mayorChangeAfterVote = addState(ChangeMayor("after vote", gameDefinition).apply { this@Day.mayorChangeEditor})
    val dayEnd = finalState("Day End")

    init {
        dayStart {
            onEntry {
                gameDefinition.dayNumber++
            }
            transition<FinishedEvent> {
                targetState = this@Day.processNightKills
            }
        }

        mayorChangeStartOfDay {
            transition<FinishedEvent> {
                targetState =this@Day.beforeVillagerKillVote
            }
        }

        beforeVillagerKillVote {
            dataTransition<VillagerVoteKillEvent, Player>("Vote for target finished") {
                targetState = this@Day.afterVillagerKillVote
            }
        }

        afterVillagerKillVote {
            onEntry {
                data.pendingKills.add(VillagerVoteKill())
            }
            transition<FinishedEvent> {
                targetState = this@Day.processDayKills
            }
        }

        ensureMayor {
            transition<RevealKillsConfirmedEvent>("Mayor is dead") {
                guard = { gameDefinition.mayor!!.alive.not() }
                targetState = this@Day.mayorChangeAfterVote
            }
            transition<RevealKillsConfirmedEvent>("Mayor is alive") {
                guard = { gameDefinition.mayor!!.alive }
                targetState = this@Day.dayEnd
            }
        }


        mayorChangeAfterVote {
            transition<FinishedEvent> {
                targetState = this@Day.dayEnd
            }
        }

    }
}
