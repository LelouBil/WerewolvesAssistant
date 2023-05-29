package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.util.BiMap
import ru.nsk.kstatemachine.DefaultState

abstract class BaseRole(val name: String) {
    abstract val winCondition: WinCondition
    abstract val participatesIn: Set<BaseCall>
}
abstract class Team(val name: String){
}

abstract class WinCondition(){
    abstract fun check(gameDefinition: GameDefinition): Boolean
}

class TeamWinCondition(private val team: Team) : WinCondition(){
    override fun check(gameDefinition: GameDefinition): Boolean {
        return gameDefinition.rolesMapping.all {
            when (val winCondition = it.value.winCondition) {
                is TeamWinCondition -> when {
                    winCondition.team != team -> it.key.alive.not()
                    else -> true
                }
                else -> false
            }
        }
    }
}

class CustomWinCondition(private val predicate: (GameDefinition) -> Boolean) : WinCondition(){
    override fun check(gameDefinition: GameDefinition): Boolean = predicate(gameDefinition)
}

abstract class BaseCall(
    name: String,
    public val mustBeBefore: Set<Class<BaseRole>> = emptySet(),
    public val mustBeAfter: Set<Class<BaseRole>> = emptySet()
) : DefaultState(name) {
    lateinit var roles: Set<BaseRole>
}
