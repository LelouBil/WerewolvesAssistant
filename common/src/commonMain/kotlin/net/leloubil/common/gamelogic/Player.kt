package net.leloubil.common.gamelogic


abstract class PendingKill() {
    fun consumed(player: Player) {}
}

class Player(name: String) {
    var alive = true
        private set

    val pendingKills = mutableListOf<PendingKill>()

    fun appendKill(kill: PendingKill) {
        pendingKills.add(kill)
    }

    fun processKills() {
        pendingKills.firstOrNull()?.let {
            it.consumed(this)
            pendingKills.remove(it)
        }
    }

}
