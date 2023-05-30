package net.leloubil.common.gamelogic

import kotlinx.coroutines.CoroutineScope

class GameDefinition(val playerList: List<Player>) {
    var mayor: Player? = null
    lateinit var buildStateMachine: suspend (CoroutineScope) -> GameStateMachineHolder
    val winners = mutableSetOf<Team>()
    var dayNumber: Int = 0
}
