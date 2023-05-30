package net.leloubil.common.gamelogic

import net.leloubil.common.gamelogic.roles.BaseRole


abstract class PendingKill() {
}

class Player(val name: String, val role: BaseRole) {
    var alive = true

    var pendingKills = mutableListOf<PendingKill>()

}
