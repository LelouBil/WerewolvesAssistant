package net.leloubil.common.gamelogic.roles

import kotlinx.coroutines.runBlocking
import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Player
import ru.nsk.kstatemachine.*

class WitchRole() : VillagerRole("Witch") {
    override val participatesIn: Set<BaseCall> = setOf(WitchCall)
    var hasHeal = true
    var hasKill = true
}

object WitchCall : BaseCall(
    name = "Witch Call"
) {
    class WitchKill : PendingKill()
    class WitchHealEvent(override val data: Player) : DataEvent<Player>
    class WitchKillEvent(override val data: Player) : DataEvent<Player>
    class WitchDoNothingEvent: Event

    init {
        runBlocking {
            val currentWitch = this@WitchCall.roles.first { it is WitchRole } as WitchRole
            lateinit var witchHeal: DataState<Player>
            lateinit var witchKill: DataState<Player>
            initialState("Before Witch choose") {
                dataTransition<WitchHealEvent, Player>("Witch heal") {
// todo                   guard { currentWitch.hasHeal }
                    targetState = witchHeal
                }
                dataTransition<WitchKillEvent, Player>("Witch kill") {
// todo                   guard { currentWitch.hasKill }
                    targetState = witchKill
                }
            }

            witchHeal = finalDataState<Player>("WitchHeal") {
                onEntry {
                    //todo verifier que c'est bien la victime des loups ou trouver un autre moyen de la récup
                    val count = data.cancelPendingKill { it is WerewolvesCall.WerewolvesKill }
                    if (count == 0) {
                        throw IllegalStateException("Witch tried to heal someone who wasn't killed by the werewolves")
                    }
                    currentWitch.hasHeal = false
                }
            }
            witchKill = finalDataState<Player>("WitchKill") {
                onEntry {
                    data.appendKill(WitchKill())
                    currentWitch.hasKill = false
                }
            }

//  todo          doNothing = finalState("WitchDoNothing")
        }
    }
}
