package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import ru.nsk.kstatemachine.onEntry

class ProcessKillsStep(name: String,gameDefinition: GameDefinition) : GameStep(name,gameDefinition) {
    init{
        onEntry {
            this.gameDefinition.playerList.forEach { it.processKills() }
        }
    }
}
