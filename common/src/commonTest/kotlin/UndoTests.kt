import io.kotest.core.spec.style.WordSpec
import io.mockk.verify
import io.mockk.verifyOrder
import net.leloubil.common.gamelogic.*
import net.leloubil.common.gamelogic.roles.VillagerRole
import net.leloubil.common.gamelogic.roles.VillagerTeam
import net.leloubil.common.gamelogic.roles.WerewolfRole
import net.leloubil.common.gamelogic.roles.WerewolvesCall
import net.leloubil.common.gamelogic.steps.ConfirmNightStartEvent
import net.leloubil.common.gamelogic.steps.NightStartState
import net.leloubil.common.gamelogic.steps.day.Day
import net.leloubil.common.gamelogic.steps.day.TryChangeMayor
import net.leloubil.common.gamelogic.steps.day.VillagerVoteStep
import net.leloubil.common.gamelogic.steps.win.ProcessKillsCheckWin

class UndoTests: WordSpec({
    "The mayor " should {
        "be reelected when undoing a mayor election" {
            lateinit var mockedDef: MutableGameDefinition
            lateinit var firstMayor: Player
            lateinit var secondMayor: Player
            testGameFlow(VillagerRole(),VillagerRole(),WerewolfRole()){
                mockedDef = gameDefinition
                firstMayor = players.living().role<VillagerRole>().first()
                secondMayor = players.living().role<WerewolfRole>().first()
                When<ShowRolesState> { ConfirmRolesEvent() }
                When<NightStartState> { ConfirmNightStartEvent() }
                When<WerewolvesCall.BeforeWereWolvesVote> {
                    WerewolvesVoteEvent(players.filterNot { it == firstMayor || it == secondMayor }.first())
                }
                When<Day.StartState> {
                    ConfirmEvent()
                }
                confirmKills(alive = listOf(VillagerRole(),WerewolfRole()), dead = listOf(VillagerRole()))
                When<TryChangeMayor.ChooseNewMayorStep>{
                    ChooseNewMayorEvent(firstMayor)
                }
                Undo(TryChangeMayor.ChooseNewMayorStep::class)
                When<TryChangeMayor.ChooseNewMayorStep>{
                    ChooseNewMayorEvent(secondMayor)
                }
                When<VillagerVoteStep.VoteState>{
                    VoteEvent(players.living().role<WerewolfRole>().first())
                }
                confirmKills(alive = listOf(VillagerRole()),dead=listOf(WerewolfRole(),VillagerRole()))
                winners(VillagerTeam)
            }
            verify(exactly = 0){
                mockedDef.mayor = not(or(isNull(),or(firstMayor,secondMayor)))
            }
            verify(exactly = 3){
                mockedDef.mayor = any()
            }
            verifyOrder {
                mockedDef.mayor = firstMayor
                mockedDef.mayor = null
                mockedDef.mayor = secondMayor
            }
        }
    }
})
