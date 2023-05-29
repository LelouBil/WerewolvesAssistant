package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Player
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass


object WerewolvesTeam : Team("Werewolves")
class WerewolfRole : BaseRole("Werewolf") {
    override val participatesIn: Set<KClass<WerewolvesCall>> = setOf(WerewolvesCall::class)
    override val winCondition: WinCondition = TeamWinCondition(WerewolvesTeam)
}

class WerewolvesCall(gameDefinition: GameDefinition) : BaseCall(
    gameDefinition,
    name = "Werewolves Call"
) {
    class WerewolvesKill : PendingKill()
    class WerewolvesVoteEvent(override val data: Player) : DataEvent<Player>

    init {
        val werewolvesVote = initialState("Start of werewolves vote");
        val werewolvesKill = finalDataState<Player>("Werewolves Killed victim")
        werewolvesVote {
            dataTransition<WerewolvesVoteEvent, Player>("Werevolves chose victim") { targetState = werewolvesKill }
        }
        werewolvesKill {
            onEntry {
                data.appendKill(WerewolvesKill())
            }
        }
    }
}

