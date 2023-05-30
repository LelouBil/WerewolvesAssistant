package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.GameStateMachineHolder
import net.leloubil.common.gamelogic.Team
import net.leloubil.common.gamelogic.steps.VillagerVoteKill
import ru.nsk.kstatemachine.onEntry
import kotlin.reflect.KClass


private const val AngelRoleName = "Angel"

object AngelTeam : Team(AngelRoleName)
class AngelRole : BaseRole(AngelRoleName) {
    override val participatesIn: Set<KClass<out BaseCall>> = emptySet()
    override val winTeam: Team = AngelTeam
    override val overrideStateMachine: (GameStateMachineHolder.() -> Unit) = {
        day.processDayKills.processKills.onEntry {
            val angelPlayer = gameDefinition.playerList.first { it.role == this@AngelRole }
            if (killMap.containsKey(angelPlayer) && killMap[angelPlayer] is VillagerVoteKill && gameDefinition.dayNumber == 1) {
                gameDefinition.winners.add(AngelTeam)
            }
        }
    }
}
