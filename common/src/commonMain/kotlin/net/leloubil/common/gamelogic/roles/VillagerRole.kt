package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.GameStateMachineHolder
import net.leloubil.common.gamelogic.Team
import kotlin.reflect.KClass

object VillagerTeam : Team("Villagers")
open class VillagerRole(name: String = "Villager") : BaseRole(name) {
    override val participatesIn: Set<KClass<out BaseCall>> = setOf()
    override val overrideStateMachine: (GameStateMachineHolder.() -> Unit)? = null
    override val winTeam: Team = VillagerTeam
}
