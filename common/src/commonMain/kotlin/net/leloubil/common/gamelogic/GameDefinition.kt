package net.leloubil.common.gamelogic

import kotlinx.coroutines.CoroutineScope
import net.leloubil.common.gamelogic.roles.BaseRole
import net.leloubil.common.gamelogic.roles.WinCondition
import net.leloubil.common.gamelogic.util.BiMap
import ru.nsk.kstatemachine.StateMachine

class GameDefinition(val rolesMapping: BiMap<Player, BaseRole>) {
    var mayor: Player? = null
    lateinit var buildStateMachine: suspend (CoroutineScope) -> StateMachine
    val playerList: List<Player> = rolesMapping.keys.toList()
    val rolesList: Set<BaseRole> = rolesMapping.values.toSet()
    val winFlags = mutableListOf<Pair<List<Player>,WinCondition>>()
}
