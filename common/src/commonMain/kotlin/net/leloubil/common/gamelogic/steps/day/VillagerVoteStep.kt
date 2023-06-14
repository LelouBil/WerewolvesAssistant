package net.leloubil.common.gamelogic.steps.day

import net.leloubil.common.gamelogic.MutableGameDefinition
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.QueueUndoEventHandler
import net.leloubil.common.gamelogic.roles.VillagerVoteKill
import net.leloubil.common.gamelogic.steps.SelfContinueDataStep
import net.leloubil.common.gamelogic.steps.SelfContinueDefaultStep
import net.leloubil.common.gamelogic.steps.selfContinuation
import ru.nsk.kstatemachine.*

open class VillagerVoteStep(private val gameDefinition: MutableGameDefinition) : DefaultState("Villager vote") {
    inner class VoteState : DefaultState("Villager Kill Vote") {
        inner class VoteEvent(override val data: Player) : DataEvent<Player>
        inner class NoKillEvent : Event

        init{
            onEntry {
                machine.processEvent(QueueUndoEventHandler.FinishedUndoEvent)
            }
        }
    }

    inner class KillState :
        SelfContinueDataStep<Player>(
            "After Villager Kill Vote",
            gameDefinition,
            dataExtractor = defaultDataExtractor(),
        ) {
        init {
            action {
                data.addPendingKill(this, VillagerVoteKill())
            }
        }
    }

    inner class NoKillState : SelfContinueDefaultStep("No kill", gameDefinition)

    init {
        val voteState = addInitialState(VoteState())
        val killState = addState(KillState())
        val noKillState = addState(NoKillState())
        val finishedState = finalState("Finished Villager Vote")

        voteState {
            dataTransition<VoteState.VoteEvent, Player>("Vote for target finished") {
                targetState = killState
            }
            transition<VoteState.NoKillEvent>("No kill") {
                targetState = noKillState
            }
        }
        killState {
            selfContinuation {
                targetState = finishedState
            }
        }
        noKillState {
            selfContinuation {
                targetState = finishedState
            }
        }
    }

}
