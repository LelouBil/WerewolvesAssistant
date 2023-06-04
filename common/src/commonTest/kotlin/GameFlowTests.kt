import io.github.aakira.napier.Napier
import io.kotest.assertions.fail
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import io.mockk.every
import net.bytebuddy.matcher.ElementMatchers.any
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.ShowRolesState
import net.leloubil.common.gamelogic.living
import net.leloubil.common.gamelogic.role
import net.leloubil.common.gamelogic.roles.*
import net.leloubil.common.gamelogic.steps.*
import net.leloubil.common.gamelogic.steps.day.*


class GameFlowTests : WordSpec({

    "The day number" When {
        "the first night ends" should {
            "be 1" {
                testGameFlow(VillagerRole(), WerewolfRole()) {
                    When<ShowRolesState> { ConfirmRolesEvent() }
                    When<NightStartState> { ConfirmNightStartEvent() }
                    When<WerewolvesCall.BeforeWereWolvesVote> {
                        WerewolvesVoteEvent(
                            players.living().role<VillagerRole>().single()
                        )
                    }
                    When<Day.StartState> {
                        gameDefinition.dayNumber shouldBe 1
                        ConfirmEvent()
                    }
                    confirmKills(alive = listOf(WerewolfRole()), dead = listOf(VillagerRole()))
                    winners(WerewolvesTeam)
                }
            }
        }
        "the second night ends" should {
            "be 2" {
                testGameFlow(VillagerRole(),VillagerRole(), WerewolfRole()) {
                    When<ShowRolesState> {
                        ConfirmRolesEvent()
                    }
                    When<NightStartState> {
                        ConfirmNightStartEvent()
                    }
                    When<WerewolvesCall.BeforeWereWolvesVote> {
                        WerewolvesVoteEvent(players.living().role<VillagerRole>().first())
                    }
                    When<Day.StartState> {
                        gameDefinition.dayNumber shouldBe 1
                        ConfirmEvent()
                    }
                    confirmKills(alive = listOf(VillagerRole(),WerewolfRole()),dead=listOf(VillagerRole()))
                    When<TryChangeMayor.ChooseNewMayorStep>{
                        ChooseNewMayorEvent(players.living().role<VillagerRole>().first())
                    }
                    When<VillagerVoteStep.VoteState>{
                        NoKillEvent()
                    }
                    confirmKills(alive = listOf(VillagerRole(),WerewolfRole()),dead=listOf(VillagerRole()))
                    When<NightStartState> {
                        ConfirmNightStartEvent()
                    }
                    When<WerewolvesCall.BeforeWereWolvesVote> {
                        WerewolvesVoteEvent(players.living().role<VillagerRole>().single())
                    }
                    When<Day.StartState> {
                        gameDefinition.dayNumber shouldBe 2
                        ConfirmEvent()
                    }
                    confirmKills(alive = listOf(WerewolfRole()), dead = listOf(VillagerRole(),VillagerRole()))
                    winners(WerewolvesTeam)
                }
            }
        }
    }

    "The win conditions" When {
        "Only villagers are present" should {
            "make the Villager win at the end of the first night" {
                testGameFlow(VillagerRole(), VillagerRole()) {
                    When<ShowRolesState> { ConfirmRolesEvent() }
                    When<NightStartState> { ConfirmNightStartEvent() }
                    When<Day.StartState> {
                        ConfirmEvent()
                    }
                    confirmKills(alive = listOf(VillagerRole(),VillagerRole()))
                    winners(VillagerTeam)
                }
            }
        }
    }
    "Only werewolves are present" should {
        "make the Werewolves win at the end of the first night" {
            testGameFlow(WerewolfRole(), WerewolfRole()) {
                When<ShowRolesState> { ConfirmRolesEvent() }
                When<NightStartState> { ConfirmNightStartEvent() }
                When<WerewolvesCall.BeforeWereWolvesVote> {
                    WerewolvesVoteEvent(players.living().role<WerewolfRole>().first())
                }
                When<Day.StartState> {
                    ConfirmEvent()
                }
                confirmKills(alive = listOf(WerewolfRole()) , dead = listOf(WerewolfRole()))
                winners(WerewolvesTeam)
            }
        }
    }
    "WereWolves and Villagers are present" should {
        "allow the werewolves to win by killing all the villagers" {
            testGameFlow(VillagerRole(), VillagerRole(), WerewolfRole()) {
                When<ShowRolesState> { ConfirmRolesEvent() }
                When<NightStartState> { ConfirmNightStartEvent() }
                When<WerewolvesCall.BeforeWereWolvesVote> {
                    WerewolvesVoteEvent(players.living().role<VillagerRole>().first())
                }
                When<Day.StartState> {
                    ConfirmEvent()
                }
                confirmKills(alive = listOf(VillagerRole(),WerewolfRole()), dead = listOf(VillagerRole()))
                When<TryChangeMayor.ChooseNewMayorStep>{
                    ChooseNewMayorEvent(players.living().role<VillagerRole>().first())
                }
                When<VillagerVoteStep.VoteState>{
                    NoKillEvent()
                }
                confirmKills(alive = listOf(VillagerRole(),WerewolfRole()),dead=listOf(VillagerRole()))
                When<NightStartState> { ConfirmNightStartEvent() }
                When<WerewolvesCall.BeforeWereWolvesVote> {
                    WerewolvesVoteEvent(players.living().role<VillagerRole>().single())
                }
                When<Day.StartState> {
                    ConfirmEvent()
                }
                confirmKills(alive = listOf(WerewolfRole()), dead = listOf(VillagerRole(),VillagerRole()))
                winners(WerewolvesTeam)
            }
        }
        "allow the villagers to win by killing all the werewolves" {
            testGameFlow(VillagerRole(), VillagerRole(), WerewolfRole()) {
                When<ShowRolesState> { ConfirmRolesEvent() }
                When<NightStartState> { ConfirmNightStartEvent() }
                When<WerewolvesCall.BeforeWereWolvesVote> {
                    WerewolvesVoteEvent(players.living().role<VillagerRole>().first())
                }
                When<Day.StartState> {
                    ConfirmEvent()
                }
                confirmKills(alive = listOf(VillagerRole(),WerewolfRole()), dead = listOf(VillagerRole()))
                When<TryChangeMayor.ChooseNewMayorStep>{
                    ChooseNewMayorEvent(players.living().role<VillagerRole>().first())
                }
                When<VillagerVoteStep.VoteState>{
                    VoteEvent(players.living().role<WerewolfRole>().single())
                }
                confirmKills(alive = listOf(VillagerRole()),dead=listOf(WerewolfRole(),VillagerRole()))
                winners(VillagerTeam)
            }
        }
    }


    "The mayor" When {
        "the first day starts" should {
            "be elected by the players" {
                lateinit var chosenMayor : Player
                val gameDef = testGameFlow(VillagerRole(), VillagerRole(), WerewolfRole()) {
                    chosenMayor = players.living().role<VillagerRole>()[1]
                    every { gameDefinition.mayor = any()} answers {
                        gameDefinition.dayNumber shouldBe 1
                        gameDefinition.mayor shouldBe null
                        callOriginal()
                    }
                    When<ShowRolesState> { ConfirmRolesEvent() }
                    When<NightStartState> { ConfirmNightStartEvent() }
                    When<WerewolvesCall.BeforeWereWolvesVote> {
                        WerewolvesVoteEvent(players.living().filterNot { it == chosenMayor }.role<VillagerRole>().first())
                    }
                    When<Day.StartState> {
                        ConfirmEvent()
                    }
                    confirmKills(alive = listOf(VillagerRole(),WerewolfRole()), dead = listOf(VillagerRole()))
                    When<TryChangeMayor.ChooseNewMayorStep>{
                        ChooseNewMayorEvent(chosenMayor)
                    }
                    When<VillagerVoteStep.VoteState>{
                        VoteEvent(players.living().role<WerewolfRole>().single())
                    }
                    confirmKills(alive = listOf(VillagerRole()),dead=listOf(WerewolfRole(),VillagerRole()))
                    winners(VillagerTeam)
                }
                verify(exactly = 1) {
                    gameDef.mayor = chosenMayor
                }
                verify(exactly = 0){
                    gameDef.mayor = not(chosenMayor)
                }
            }
        }
        "the mayor dies" should {
            "be elected by the players" {
                lateinit var chosenMayor : Player
                lateinit var secondMayor : Player
                val gameDef = testGameFlow(VillagerRole(), VillagerRole(),VillagerRole(), WerewolfRole()) {
                    val villagers = players.living().role<VillagerRole>()
                    chosenMayor = villagers[1]
                    secondMayor = villagers[2]
                    every { gameDefinition.mayor = any()} answers {
                        gameDefinition.dayNumber shouldBe 1
                        gameDefinition.mayor shouldBe null
                        callOriginal()
                    } andThenAnswer {
                        gameDefinition.mayor shouldBe chosenMayor
                        callOriginal()
                    }
                    When<ShowRolesState> { ConfirmRolesEvent() }
                    When<NightStartState> { ConfirmNightStartEvent() }
                    When<WerewolvesCall.BeforeWereWolvesVote> {
                        WerewolvesVoteEvent(players.living().filterNot { it == chosenMayor || it == secondMayor }.role<VillagerRole>().first())
                    }
                    When<Day.StartState> {
                        ConfirmEvent()
                    }
                    confirmKills(alive = listOf(VillagerRole(),VillagerRole(),WerewolfRole()), dead = listOf(VillagerRole()))
                    When<TryChangeMayor.ChooseNewMayorStep>{
                        ChooseNewMayorEvent(chosenMayor)
                    }
                    When<VillagerVoteStep.VoteState>{
                        VoteEvent(chosenMayor)
                    }
                    confirmKills(alive = listOf(VillagerRole(),WerewolfRole()),dead=listOf(VillagerRole(),VillagerRole()))
                    When<TryChangeMayor.ChooseNewMayorStep>{
                        ChooseNewMayorEvent(secondMayor)
                    }
                    When<NightStartState> { ConfirmNightStartEvent() }
                    When<WerewolvesCall.BeforeWereWolvesVote> {
                        WerewolvesVoteEvent(secondMayor)
                    }
                    When<Day.StartState> {
                        ConfirmEvent()
                    }
                    confirmKills(alive = listOf(WerewolfRole()), dead = listOf(VillagerRole(),VillagerRole(),VillagerRole()))
                    winners(WerewolvesTeam)
                }
                verifyOrder{
                    gameDef.mayor = chosenMayor
                    gameDef.mayor = secondMayor
                }
                verify(exactly = 1){
                    gameDef.mayor = chosenMayor
                }
                verify(exactly = 1){
                    gameDef.mayor = secondMayor
                }
                gameDef.mayor shouldBe secondMayor
            }
        }
        "the second day starts" should {
            "not be elected by the players if alive" {
                lateinit var chosenMayor : Player
                val gameDef = testGameFlow(VillagerRole(),VillagerRole(), VillagerRole(), WerewolfRole()) {
                    chosenMayor = players.living().role<VillagerRole>()[1]
                    When<ShowRolesState> { ConfirmRolesEvent() }
                    When<NightStartState> { ConfirmNightStartEvent() }
                    When<WerewolvesCall.BeforeWereWolvesVote> {
                        WerewolvesVoteEvent(players.living().filterNot { it == chosenMayor }.role<VillagerRole>().first())
                    }
                    When<Day.StartState> {
                        ConfirmEvent()
                    }
                    confirmKills(alive = listOf(VillagerRole(),VillagerRole(),WerewolfRole()), dead = listOf(VillagerRole()))
                    When<TryChangeMayor.ChooseNewMayorStep>{
                        ChooseNewMayorEvent(chosenMayor)
                    }
                    When<VillagerVoteStep.VoteState>{
                        VoteEvent(players.living().filterNot { it == chosenMayor }.role<VillagerRole>().first())
                    }
                    confirmKills(alive = listOf(VillagerRole(),WerewolfRole()),dead=listOf(VillagerRole(),VillagerRole()))
                    When<NightStartState> { ConfirmNightStartEvent() }
                    When<WerewolvesCall.BeforeWereWolvesVote> {
                        WerewolvesVoteEvent(players.living().role<WerewolfRole>().first())
                    }
                    When<Day.StartState> {
                        ConfirmEvent()
                    }
                    confirmKills(alive = listOf(VillagerRole()), dead = listOf(VillagerRole(),VillagerRole(),WerewolfRole()))
                    winners(VillagerTeam)
                }
                verify(exactly = 1) {
                    gameDef.mayor = any()
                }
            }
        }
    }
}
)
