package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.roles.TeamWinCondition
import net.leloubil.common.gamelogic.roles.WinCondition
import ru.nsk.kstatemachine.*

typealias PlayerWinConditionPair = Pair<Player,WinCondition>

class CheckWinStep(name: String,gameDefinition: GameDefinition, gameEndState: State, continueState: State) : GameStep(name, gameDefinition) {

    init{
        onEntry {
            //todo refactor win

        }
        transition<FinishedEvent>("If at least one win condition is met"){
            guard = { gameDefinition.winFlags.isNotEmpty() }
            targetState = gameEndState
        }
        transition<FinishedEvent>("If no win condition is met"){
            guard = { gameDefinition.winFlags.isEmpty() }
            targetState = continueState
        }
    }
}
