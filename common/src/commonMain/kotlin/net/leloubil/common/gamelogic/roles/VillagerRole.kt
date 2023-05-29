package net.leloubil.common.gamelogic.roles

object VillagerTeam : Team("Villagers")
abstract class VillagerRole(name: String) : BaseRole(name) {
    override val winCondition: WinCondition = TeamWinCondition(VillagerTeam)
}
