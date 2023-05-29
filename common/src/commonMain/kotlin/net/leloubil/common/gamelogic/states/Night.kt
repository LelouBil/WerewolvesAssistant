package net.leloubil.common.gamelogic.states

import net.leloubil.common.gamelogic.roles.BaseRole
import ru.nsk.kstatemachine.*


class Night(private val rolesList: Set<BaseRole>) : DefaultState("Night") {
    init {
        lateinit var nightEnd: State
        val callList = rolesList.map { it.participatesIn }.flatten().distinct()
        callList.forEach{ it.roles = rolesList }
        //todo sort using mustBeBefore and mustBeAfter
        callList.chunked(2).forEach {
            val transition: IState.() -> Unit =
                if (it.size == 2) {
                    {
                        transition<FinishedEvent>() { targetState = it[1] }
                    }
                } else {
                    {
                        transition<FinishedEvent>() { targetState = nightEnd }
                    }
                }
            addState(it[0], transition)
        }
        nightEnd = finalState("Night End")
    }
}
