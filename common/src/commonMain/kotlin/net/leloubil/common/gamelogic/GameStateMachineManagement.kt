package net.leloubil.common.gamelogic

import kotlinx.coroutines.runBlocking
import net.leloubil.common.gamelogic.roles.BaseRole
import net.leloubil.common.gamelogic.util.BiMap
import net.leloubil.common.gamelogic.util.HashBiMap
import ru.nsk.kstatemachine.StateMachine
import ru.nsk.kstatemachine.createStateMachine
import ru.nsk.kstatemachine.initialState
import ru.nsk.kstatemachine.state


public fun createGameStateMachine(
    rolesList: Set<BaseRole>
): StateMachine =
    runBlocking {
        createStateMachine(
            scope = this,
            name = "Game State Machine",
            enableUndo = true,
            doNotThrowOnMultipleTransitionsMatch = false
        ) {
            initialState("Night") {
                initialState("Night Start")
                state("Night End")
            }

        }
    }


public fun createGameDefinition(
    playerList: List<Player>,
    rolesList: Set<BaseRole>
) : GameDefinition {
    val rolesMapping: BiMap<Player, BaseRole> = HashBiMap.create(playerList.shuffled().zip(rolesList.shuffled()).toMap())
    val stateMachine = createGameStateMachine(rolesList)
    return GameDefinition(rolesMapping, stateMachine)
}

class GameDefinition(val rolesMapping: BiMap<Player, BaseRole>, val stateMachine: StateMachine) {
    val playerList: List<Player> = rolesMapping.keys.toList()
    val rolesList: Set<BaseRole> = rolesMapping.values.toSet()
}
