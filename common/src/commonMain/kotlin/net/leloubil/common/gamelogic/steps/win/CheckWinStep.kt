package net.leloubil.common.gamelogic.steps.win

import net.leloubil.common.gamelogic.MutableGameDefinition
import net.leloubil.common.gamelogic.steps.SelfContinueDefaultStep
import net.leloubil.common.gamelogic.steps.selfContinuation
import ru.nsk.kstatemachine.State


open class CheckWinStep(name: String, gameDefinition: MutableGameDefinition, gameEndState: State, continueState: State) :
    SelfContinueDefaultStep(name, gameDefinition) {

    init {
        action {
            gameDefinition.playerList.filter { it.alive }.forEach { player ->
                if (gameDefinition.playerList.filter { it.alive }.all { it.role.winTeam == player.role.winTeam }) {
                    gameDefinition::winners undoAssign gameDefinition.winners + player.role.winTeam
                }
            }
        }
        selfContinuation("If there is at least one winner $name") {
            guard = { gameDefinition.winners.isNotEmpty() }
            targetState = gameEndState
        }
        selfContinuation("If there is no winner $name") {
            guard = { gameDefinition.winners.isEmpty() }
            targetState = continueState
        }
    }
}
