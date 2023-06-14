package net.leloubil.common.gamelogic

import net.leloubil.common.gamelogic.roles.BaseRole
import kotlin.reflect.KClass


abstract class PendingKill

class Player(val name: String, val role: BaseRole) {
    var alive = true
        private set
    private var pendingKills = listOf<PendingKill>()

    fun addPendingKill(undoScope: DoActionableStepScope,kill: PendingKill) {
        if (!alive) throw IllegalStateException("Cannot kill a dead player")
        if(pendingKills.filterIsInstance(kill::class.java).any())
            throw IllegalStateException("This player already has a pending kill of type ${kill::class.simpleName}")
        with(undoScope) {
            ::pendingKills undoAssign pendingKills + kill
        }
    }

    fun removePendingKill(undoScope: DoActionableStepScope,killType: KClass<out PendingKill>) {
        if(pendingKills.none { it::class == killType })
            throw IllegalStateException("This player doesn't have a pending kill of type ${killType.simpleName}")
        with(undoScope) {
            ::pendingKills undoAssign pendingKills.filter { it::class != killType }
        }
    }

    fun applyPendingKills(undoScope: DoActionableStepScope) : PendingKill? {
        with(undoScope) {
            return pendingKills.firstOrNull()?.apply {
                ::alive undoAssign  false
                ::pendingKills undoAssign listOf()
            }
        }
    }

    override fun toString(): String {
        return "\"$name\"(${role::class.simpleName} - ${if (alive) "alive" else "dead"})"
    }
}

fun List<Player>.living() = filter { it.alive }

fun List<Player>.dead() = filter { !it.alive }

inline fun <reified T : BaseRole> List<Player>.role() = filter { it.role is T }
