package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.*
import net.leloubil.common.gamelogic.steps.SelfContinueDataStep
import net.leloubil.common.gamelogic.steps.selfContinuation
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass


object WerewolvesTeam : Team()

class WerewolvesKill : PendingKill()
class WerewolfRole : BaseRole() {
    override val winTeam = WerewolvesTeam
    override val participatesIn: Set<KClass<out BaseCall>> = setOf(WerewolvesCall::class)
    override val overrideStateMachine: (StateMachine.() -> Unit)? = null
}

class WerewolvesCall(gameDefinition: MutableGameDefinition) : BaseCall(
    gameDefinition,
    name = "Werewolves Call"
) {
    inner class BeforeWereWolvesVote : DefaultState("Before werewolves vote"){
        inner class WerewolvesVoteEvent(override val data: Player) : DataEvent<Player>
        init{
            onEntry {
                machine.processEvent(QueueUndoEventHandler.FinishedUndoEvent)
            }
        }
    }
    inner class WerewolvesKillState : SelfContinueDataStep<Player>("Werewolves kill victim", gameDefinition, dataExtractor = defaultDataExtractor())

    init {
        val werewolvesVote = addInitialState(BeforeWereWolvesVote())
        val werewolvesKill = addState(WerewolvesKillState())
        val werewolvesEnd = finalState("Werewolves finished")
        werewolvesVote {
            dataTransition<BeforeWereWolvesVote.WerewolvesVoteEvent, Player>("Werewolves chose victim") { targetState = werewolvesKill }
        }
        werewolvesKill {
            action {
                data.addPendingKill(this,WerewolvesKill())
            }
            selfContinuation {
                targetState = werewolvesEnd
            }
        }
    }
}

