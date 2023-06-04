package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.PendingKill
import net.leloubil.common.gamelogic.Team
import ru.nsk.kstatemachine.StateMachine
import kotlin.reflect.KClass

object VillagerTeam : Team()

class VillagerVoteKill : PendingKill()

open class VillagerRole : BaseRole() {
    override val participatesIn: Set<KClass<out BaseCall>> = setOf()
    override val overrideStateMachine: (StateMachine.() -> Unit)? = null
    override val winTeam: Team = VillagerTeam

}
