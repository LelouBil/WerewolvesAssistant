package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.MutableGameDefinition
import net.leloubil.common.gamelogic.Team
import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.IState
import ru.nsk.kstatemachine.StateMachine
import kotlin.reflect.KClass


abstract class BaseRole() {
    abstract val participatesIn: Set<KClass<out BaseCall>>
    abstract val overrideStateMachine: (StateMachine.() -> Unit)?
    abstract val winTeam: Team

    protected fun <T : IState> IState.editState(stateType: KClass<T>, lambda: T.() -> Unit): Int {
        val stateList = states.filterIsInstance(stateType.java)
        stateList.map { lambda(it) }
        return states.sumOf { it.editState(stateType, lambda) } + stateList.size
    }

}


abstract class BaseCall(
    protected val gameDefinition: MutableGameDefinition,
    name: String
) : DefaultState(name)
