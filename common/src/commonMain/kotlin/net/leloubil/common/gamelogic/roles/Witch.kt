package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Player
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass

class WitchRole : VillagerRole() {
    override val participatesIn: Set<KClass<WitchCall>> = setOf(WitchCall::class)
    override val overrideStateMachine: (StateMachine.() -> Unit)? = null
    var hasHeal = true
    var hasKill = true
}

class WitchCall(gameDefinition: GameDefinition) : BaseCall(
    gameDefinition,
    name = "Witch Call"
) {
    class WitchKill : PendingKill()
    class WitchHealEvent(override val data: Player) : DataEvent<Player>
    class WitchKillEvent(override val data: Player) : DataEvent<Player>
    class WitchDoNothingEvent : Event

    init {
        val witch = this.gameDefinition.playerList.single { it.role is WitchRole }.role as WitchRole

        val beforeWitchChoice = initialState("Before Witch choose")

        val witchHeal = finalDataState<Player>("WitchHeal")
        val witchKill = finalDataState<Player>("WitchKill")
        val doNothing = finalState("WitchDoNothing")


        beforeWitchChoice {
            dataTransition<WitchHealEvent, Player>("If witch has heal and wants to use it") {
                guard = { witch.hasHeal }
                targetState = witchHeal
            }
            dataTransition<WitchKillEvent, Player>("If witch has kill and wants to use it") {
                guard = { witch.hasKill }
                targetState = witchKill
            }
            transition<WitchDoNothingEvent>("if witch can't do anything, or chooses to do nothing") {
                targetState = doNothing
            }
        }

        witchHeal {
            onEntry {
                data.removePendingKill(WerewolvesKill::class)
                witch.hasHeal = false
            }
        }
        witchKill {
            onEntry {
                data.addPendingKill(WitchKill())
                witch.hasKill = false
            }
        }

    }
}
