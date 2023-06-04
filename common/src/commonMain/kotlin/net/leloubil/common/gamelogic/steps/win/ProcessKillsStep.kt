package net.leloubil.common.gamelogic.steps.win

import io.github.aakira.napier.Napier
import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.living
import ru.nsk.kstatemachine.DefaultState
import ru.nsk.kstatemachine.onEntry

open class ProcessKillsStep(name: String, val gameDefinition: GameDefinition) : DefaultState(name) {
    val killMap: MutableMap<Player, PendingKill> = mutableMapOf()

    init {
        onEntry {
            killMap.clear()
            this.gameDefinition.playerList.living().forEach {
                val kill = it.applyPendingKills()
                if (kill != null) {
                    killMap[it] = kill
                }
            }
        }
    }
}
