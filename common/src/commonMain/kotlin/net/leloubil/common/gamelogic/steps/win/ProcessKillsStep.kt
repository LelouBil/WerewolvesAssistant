package net.leloubil.common.gamelogic.steps.win

import net.leloubil.common.gamelogic.*

open class ProcessKillsStep(name: String, val gameDefinition: MutableGameDefinition) : ActionableDefaultStep(
    name,
    gameDefinition
) {
    var killMap: Map<Player, PendingKill> = emptyMap()
        private set

    init {
        action {
            ::killMap undoAssign this@ProcessKillsStep.gameDefinition.playerList.living().mapNotNull {
                val kill = it.applyPendingKills(this)
                if (kill != null) {
                    it to kill
                } else {
                    null
                }
            }.associate { it }

        }
    }
}
