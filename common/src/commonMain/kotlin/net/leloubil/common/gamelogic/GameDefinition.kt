package net.leloubil.common.gamelogic

import kotlinx.coroutines.CoroutineScope
import ru.nsk.kstatemachine.StateMachine

class GameDefinition(val playerList: List<Player>) {
    var mayor: Player? = null
    lateinit var stateMachine: StateMachine
    val winners = mutableSetOf<Team>()
    var dayNumber: Int = 0
}
