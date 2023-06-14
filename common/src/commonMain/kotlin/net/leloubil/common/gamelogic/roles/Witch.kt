package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.MutableGameDefinition
import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.steps.SelfContinueDataStep
import net.leloubil.common.gamelogic.steps.SelfContinueDefaultStep
import net.leloubil.common.gamelogic.steps.selfContinuation
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass

class WitchRole : VillagerRole() {
    override val participatesIn: Set<KClass<WitchCall>> = setOf(WitchCall::class)
    override val overrideStateMachine: (StateMachine.() -> Unit)? = null
    var hasHeal = true
    var hasKill = true
}

class WitchCall(gameDefinition: MutableGameDefinition) : BaseCall(
    gameDefinition,
    name = "Witch Call"
) {
    class WitchKill : PendingKill()

    inner class BeforeWitchChoiceState : DefaultState("Before Witch choose") {
        inner class WitchHealEvent(override val data: Player) : DataEvent<Player>
        inner class WitchKillEvent(override val data: Player) : DataEvent<Player>
        inner class WitchDoNothingEvent : Event
    }

    inner class WitchHealState :
        SelfContinueDataStep<Player>("WitchHeal", gameDefinition, dataExtractor = defaultDataExtractor())

    inner class WitchKillState :
        SelfContinueDataStep<Player>("WitchKill", gameDefinition, dataExtractor = defaultDataExtractor())

    inner class WitchDoNothingState : SelfContinueDefaultStep("WitchDoNothing", gameDefinition)

    init {
        val witch = this.gameDefinition.playerList.single { it.role is WitchRole }.role as WitchRole

        val beforeWitchChoice = initialState("Before Witch choose")

        val witchHeal = addState(WitchHealState())
        val witchKill = addState(WitchKillState())
        val doNothing = addState(WitchDoNothingState())
        val finished = finalState("WitchFinished")


        beforeWitchChoice {
            dataTransition<BeforeWitchChoiceState.WitchHealEvent, Player>("If witch has heal and wants to use it") {
                guard = { witch.hasHeal }
                targetState = witchHeal
            }
            dataTransition<BeforeWitchChoiceState.WitchKillEvent, Player>("If witch has kill and wants to use it") {
                guard = { witch.hasKill }
                targetState = witchKill
            }
            transition<BeforeWitchChoiceState.WitchDoNothingEvent>("if witch can't do anything, or chooses to do nothing") {
                targetState = doNothing
            }
        }

        witchHeal {
            action {
                data.removePendingKill(this,WerewolvesKill::class)
                witch::hasHeal undoAssign false
            }
            selfContinuation {
                targetState = finished
            }
        }
        witchKill {
            action {
                data.addPendingKill(this,WitchKill())
                witch.hasKill = false
            }
            selfContinuation {
                targetState = finished
            }
        }

    }
}
