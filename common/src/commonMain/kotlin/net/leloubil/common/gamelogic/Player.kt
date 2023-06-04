package net.leloubil.common.gamelogic

import net.leloubil.common.gamelogic.roles.BaseRole
import kotlin.reflect.KClass


abstract class PendingKill

class Player(val name: String, val role: BaseRole) {
    var alive = true
    private val pendingKills = mutableListOf<PendingKill>()

    fun addPendingKill(kill: PendingKill) {
        if (!alive) throw IllegalStateException("Cannot kill a dead player")
        if(pendingKills.filterIsInstance(kill::class.java).any())
            throw IllegalStateException("This player already has a pending kill of type ${kill::class.simpleName}")
        pendingKills.add(kill)
    }

    fun removePendingKill(killType: KClass<out PendingKill>) {
        val anyRemoved = pendingKills.removeIf { it::class == killType }
        if (!anyRemoved) throw IllegalStateException("This player doesn't have a pending kill of type ${killType.simpleName}")
    }

    fun applyPendingKills() : PendingKill? {
        return pendingKills.firstOrNull()?.apply {
            alive = false
            pendingKills.clear()
        }
    }

    override fun toString(): String {
        return "\"$name\"(${role::class.simpleName} - ${if (alive) "alive" else "dead"})"
    }
}

fun List<Player>.living() = filter { it.alive }

fun List<Player>.dead() = filter { !it.alive }

inline fun <reified T : BaseRole> List<Player>.role() = filter { it.role is T }
