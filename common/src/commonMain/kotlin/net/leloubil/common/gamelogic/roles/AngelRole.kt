package net.leloubil.common.gamelogic.roles

import net.leloubil.common.gamelogic.Team
import net.leloubil.common.gamelogic.steps.win.ProcessKillsCheckWin
import ru.nsk.kstatemachine.StateMachine
import ru.nsk.kstatemachine.onEntry
import kotlin.reflect.KClass



object AngelTeam : Team()
class AngelRole : BaseRole() {
    override val participatesIn: Set<KClass<out BaseCall>> = emptySet()
    override val winTeam: Team = AngelTeam
    override val overrideStateMachine: (StateMachine.() -> Unit) = {
        editState(ProcessKillsCheckWin.ProcessKillsStepPart::class) {
            action {
                val angelPlayer = gameDefinition.playerList.first { it.role == this@AngelRole }
                if (killMap.containsKey(angelPlayer) && killMap[angelPlayer] is VillagerVoteKill && gameDefinition.dayNumber == 1) {
                    gameDefinition::winners undoAssign gameDefinition.winners + AngelTeam
                }
            }
        }
    }
}
