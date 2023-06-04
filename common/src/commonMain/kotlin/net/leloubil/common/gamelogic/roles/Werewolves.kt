package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.*
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass


object WerewolvesTeam : Team()

class WerewolvesKill : PendingKill()
class WerewolfRole : BaseRole() {
    override val winTeam = WerewolvesTeam
    override val participatesIn: Set<KClass<out BaseCall>> = setOf(WerewolvesCall::class)
    override val overrideStateMachine: (StateMachine.() -> Unit)? = null
}

class WerewolvesCall(gameDefinition: GameDefinition) : BaseCall(
    gameDefinition,
    name = "Werewolves Call"
) {
    inner class BeforeWereWolvesVote : DefaultState("Before werewolves vote"){
        inner class WerewolvesVoteEvent(override val data: Player) : DataEvent<Player>
    }

    init {
        val werewolvesVote = addInitialState(BeforeWereWolvesVote())
        val werewolvesKill = finalDataState<Player>("Werewolves Killed victim")
        werewolvesVote {
            dataTransition<BeforeWereWolvesVote.WerewolvesVoteEvent, Player>("Werewolves chose victim") { targetState = werewolvesKill }
        }
        werewolvesKill {
            onEntry {
                data.addPendingKill(WerewolvesKill())
            }
        }
    }
}

