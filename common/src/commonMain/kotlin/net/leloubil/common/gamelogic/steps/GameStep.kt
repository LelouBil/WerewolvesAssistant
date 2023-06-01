package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import ru.nsk.kstatemachine.*

open class GameStep(name: String, val gameDefinition: GameDefinition) : DefaultState(name)

open class SelfContinueGameStep(name: String, gameDefinition: GameDefinition) : GameStep(name, gameDefinition) {
    inner class SelfContinueEvent : Event

    init{
        gameDefinition.stateMachineHolder.whenBuildFinished.add{
            onEntry {
                gameDefinition.stateMachineHolder.stateMachine.processEvent(SelfContinueEvent())
            }
        }
    }

    fun selfContinuation(lambda: UnitGuardedTransitionBuilder<SelfContinueEvent>.() -> Unit) = transition<SelfContinueEvent>(block = lambda)
    fun selfContinuation(
        transitionName: String?,
        lambda: UnitGuardedTransitionBuilder<SelfContinueEvent>.() -> Unit
    ) = transition<SelfContinueEvent>(name = transitionName,block = lambda)
}
