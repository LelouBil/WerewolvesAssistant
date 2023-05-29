package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.GameDefinition
import ru.nsk.kstatemachine.DefaultState
import kotlin.reflect.KClass

abstract class BaseRole(val name: String) {
    abstract val winCondition: WinCondition
    abstract val participatesIn: Set<KClass<out BaseCall>>
}

abstract class Team(val name: String) {
}

abstract class WinCondition() {
    abstract fun check(gameDefinition: GameDefinition): Boolean
}
//todo refactor les wincondition, surtout que les roles pourront intercepter des states
// pour l'ange par ex, qui pourra faire day.afterVillagerKillVote.onEntry { gameDefinition.winFlags.add(AngelWinCondition) }
// plus ou moins comme ça

//todo mais pour ca faut que dans toutes les Step, les sous states soient des champs, et pas des variables locales
class TeamWinCondition(private val team: Team) : WinCondition() {
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

class CustomWinCondition(private val predicate: (GameDefinition) -> Boolean) : WinCondition() {
    override fun check(gameDefinition: GameDefinition): Boolean = predicate(gameDefinition)
}

abstract class BaseCall(
    protected val gameDefinition: GameDefinition,
    name: String
) : DefaultState(name) {
}
