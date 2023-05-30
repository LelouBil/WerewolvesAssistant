package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.roles.BaseRole
import ru.nsk.kstatemachine.onEntry

class ProcessKillsStep(name: String, gameDefinition: GameDefinition) : GameStep(name, gameDefinition) {
    val killMap: MutableMap<Player, PendingKill> = mutableMapOf()

    init {
        onEntry {
            killMap.clear()
            while (this.gameDefinition.playerList.any { it.pendingKills.isNotEmpty() }) {
                this.gameDefinition.playerList.forEach {
                    it.pendingKills.firstOrNull()?.let { kill ->
                        it.alive = false
                        it.pendingKills.remove(kill)
                        killMap[it] = kill
                    }
                }
            }
        }
    }
}
