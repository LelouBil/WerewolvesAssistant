package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.Team

object VillagerTeam : Team("Villagers")
abstract class VillagerRole(name: String) : BaseRole(name) {
    override val winTeam: Team = VillagerTeam
}
