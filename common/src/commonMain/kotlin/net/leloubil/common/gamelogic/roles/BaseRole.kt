package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.GameStateMachineHolder
import net.leloubil.common.gamelogic.Team
import ru.nsk.kstatemachine.DefaultState
import kotlin.reflect.KClass


abstract class BaseRole(val name: String) {
    abstract val participatesIn: Set<KClass<out BaseCall>>
    abstract val overrideStateMachine: (GameStateMachineHolder.() -> Unit)?
    abstract val winTeam: Team
}


abstract class BaseCall(
    protected val gameDefinition: GameDefinition,
    name: String
) : DefaultState(name)
