package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import ru.nsk.kstatemachine.FinishedEvent
import ru.nsk.kstatemachine.State
import ru.nsk.kstatemachine.onEntry
import ru.nsk.kstatemachine.transition


class CheckWinStep(name: String, gameDefinition: GameDefinition, gameEndState: State, continueState: State) :
    SelfContinueGameStep(name, gameDefinition) {

    init {
        onEntry {
            gameDefinition.playerList.filter { it.alive }.forEach { player ->
                if (gameDefinition.playerList.filter { it.alive }.all { it.role.winTeam == player.role.winTeam }) {
                    gameDefinition.winners.add(player.role.winTeam)
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
