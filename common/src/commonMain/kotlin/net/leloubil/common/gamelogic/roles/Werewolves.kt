package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Player
import ru.nsk.kstatemachine.*


object WerewolvesTeam : Team("Werewolves")
class WerewolfRole : BaseRole("Werewolf") {
    override val participatesIn: Set<BaseCall> = setOf(WerewolvesCall)
    override val winCondition: WinCondition = TeamWinCondition(WerewolvesTeam)
}

object WerewolvesCall : BaseCall(
    name = "Werewolves Call"
    )
{
    class WerewolvesKill : PendingKill()
    class WerewolvesVoteEvent(override val data: Player) : DataEvent<Player>
    init{
        lateinit var werewolvesKill: DataState<Player>
        initialState("Before Werewolves Vote"){
            dataTransition<WerewolvesVoteEvent,Player>("Werewolves vote finish") { targetState = werewolvesKill }
        }
        werewolvesKill = finalDataState<Player>("Werewolves Kill") {
            onEntry {
                data.appendKill(WerewolvesKill())
            }
        }
    }
}
