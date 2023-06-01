package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.*
import net.leloubil.common.gamelogic.steps.GameStep
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass


object WerewolvesTeam : Team("Werewolves")

class WerewolfRole : BaseRole("Werewolf") {
    override val winTeam = WerewolvesTeam
    override val participatesIn: Set<KClass<out BaseCall>> = setOf(WerewolvesCall::class)
    override val overrideStateMachine: (GameStateMachineHolder.() -> Unit)? = null
}

class WerewolvesCall(gameDefinition: GameDefinition) : BaseCall(
    gameDefinition,
    name = "Werewolves Call"
) {
    class WerewolvesKill : PendingKill()
    class WerewolvesVoteEvent(override val data: Player) : DataEvent<Player>

    class BeforeWereWolvesVote(gameDefinition: GameDefinition) : GameStep("Before werewolves vote", gameDefinition)

    init {
        val werewolvesVote = addInitialState(BeforeWereWolvesVote(gameDefinition))
        val werewolvesKill = finalDataState<Player>("Werewolves Killed victim")
        werewolvesVote {
            dataTransition<WerewolvesVoteEvent, Player>("Werewolves chose victim") { targetState = werewolvesKill }
        }
        werewolvesKill {
            onEntry {
                data.pendingKills.add(WerewolvesKill())
            }
        }
    }
}

