package net.leloubil.common.gamelogic

import ru.nsk.kstatemachine.StateMachine

class MutableGameDefinition(override val playerList: List<Player>): ReadOnlyGameDefinition {
    override var mayor: Player? = null
    override lateinit var stateMachine: StateMachine
    override var winners = setOf<Team>()
    override var dayNumber: Int = 0
    var isUndoing: Boolean = false
}
//todo disable access to statemachine, only method for sending event and callback for state change
interface ReadOnlyGameDefinition{
    val playerList: List<Player>
    val mayor: Player?
    val stateMachine: StateMachine
    val winners: Set<Team>
    val dayNumber: Int
}
