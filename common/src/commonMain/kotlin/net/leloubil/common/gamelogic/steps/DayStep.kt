package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Player
import ru.nsk.kstatemachine.*

class VillagerVoteMayorEvent(override val data: Player) : DataEvent<Player>
class VillagerVoteKillEvent(override val data: Player) : DataEvent<Player>
class VillagerKill : PendingKill()
class Day(gameDefinition: GameDefinition, gameEndState: State) : GameStep("Day", gameDefinition) {

    init{
        val dayStart = initialState("Day Start")
        val dayEnd = finalState("Day End")
        val beforeMayorVote = state("Mayor Vote Start")
        val afterMayorVote = dataState<Player>("Mayor Chosen")
        val beforeVillagerKillVote = state("Villager Vote Start")
        val afterVillagerKillVote = dataState<Player>("Villager Killed")
        val processKillsStep = addState(ProcessKillsStep("Process Kills Day",gameDefinition))
        val checkWin = addState(CheckWinStep("Check Win Day",gameDefinition,
            gameEndState = gameEndState,
            continueState = dayEnd))

        dayStart {
            transition<FinishedEvent>("If mayor is not elected") {
                guard = { gameDefinition.mayor == null}
                targetState = beforeMayorVote
            }
            transition<FinishedEvent>("If mayor is already elected") {
                guard = { gameDefinition.mayor != null}
                targetState = beforeVillagerKillVote
            }
        }

        beforeMayorVote{
            dataTransition<VillagerVoteMayorEvent,Player>("Mayor Vote received") {
                targetState = afterMayorVote
            }
        }

        afterMayorVote{
            onEntry {
                gameDefinition.mayor = data
            }
            transition<FinishedEvent>{
                targetState = beforeVillagerKillVote
            }
        }

        beforeVillagerKillVote{
            dataTransition<VillagerVoteKillEvent,Player>("Vote for target finished") {
                targetState = afterVillagerKillVote
            }
        }

        afterVillagerKillVote{
            onEntry {
                data.appendKill(VillagerKill())
            }
            transition<FinishedEvent>{
                targetState = processKillsStep
            }
        }

        processKillsStep{
            transition<FinishedEvent>{
                targetState = checkWin
            }
        }

    }
}
