package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.*
import ru.nsk.kstatemachine.*

class SelfContinueEvent : Event

interface SelfContinueGameStep : IState
open class SelfContinueDefaultStep(name: String, private val gameDefinition: MutableGameDefinition) : ActionableDefaultStep(name,gameDefinition), SelfContinueGameStep{
    override val listeners: Collection<IState.Listener>
        get() = super.listeners +  object : IState.Listener {
            override suspend fun onEntry(transitionParams: TransitionParams<*>) {
                gameDefinition.stateMachine.processEvent(SelfContinueEvent())
            }
        }
}

open class SelfContinueDataStep<D : Any>(
    name: String,
    private val gameDefinition: MutableGameDefinition,
    dataExtractor: DataExtractor<D>,
) : ActionableDataStep<D>(name, gameDefinition,dataExtractor = dataExtractor), SelfContinueGameStep {

    override val listeners: Collection<IState.Listener>
        get() = super.listeners + object : IState.Listener {
            override suspend fun onEntry(transitionParams: TransitionParams<*>) {
                gameDefinition.stateMachine.processEvent(SelfContinueEvent())
            }
        }
}

fun SelfContinueGameStep.selfContinuation(lambda: UnitGuardedTransitionBuilder<SelfContinueEvent>.() -> Unit) =
    transition<SelfContinueEvent>(block = lambda)

fun SelfContinueGameStep.selfContinuation(
    transitionName: String?,
    lambda: UnitGuardedTransitionBuilder<SelfContinueEvent>.() -> Unit
) = transition<SelfContinueEvent>(name = transitionName, block = lambda)
