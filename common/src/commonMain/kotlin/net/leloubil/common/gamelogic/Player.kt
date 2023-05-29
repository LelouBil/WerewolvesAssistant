package net.leloubil.common.gamelogic


abstract class PendingKill() {
    fun consumed(player: Player) {}
}

class Player(name: String) {
    var alive = true
        private set

    private val pendingKills = mutableListOf<PendingKill>()

    fun appendKill(kill: PendingKill) {
        pendingKills.add(kill)
    }

    fun processKills() {
        pendingKills.firstOrNull()?.let {
            it.consumed(this)
            pendingKills.remove(it)
        }
    }
     fun cancelPendingKill(predicate: (PendingKill) -> Boolean): Int {
        val filter = pendingKills.filter { predicate(it) }
        val count = filter.count()
        filter.forEach { pendingKills.remove(it) }
        return count
    }

}
