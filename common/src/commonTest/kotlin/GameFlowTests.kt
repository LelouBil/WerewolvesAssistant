import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.kotest.assertions.fail
import io.kotest.assertions.withClue
import io.kotest.core.NamedTag
import io.kotest.core.annotation.Tags
import io.kotest.core.extensions.RuntimeTagExpressionExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.types.shouldBeTypeOf
import kotlinx.coroutines.runBlocking
import net.leloubil.common.CustomAntiLog
import net.leloubil.common.gamelogic.ConfirmRolesEvent
import net.leloubil.common.gamelogic.GameEndState
import net.leloubil.common.gamelogic.ShowRolesState
import net.leloubil.common.gamelogic.createGameDefinition
import net.leloubil.common.gamelogic.roles.VillagerRole
import net.leloubil.common.gamelogic.roles.WerewolfRole
import net.leloubil.common.gamelogic.roles.WerewolvesCall
import net.leloubil.common.gamelogic.roles.WerewolvesTeam
import net.leloubil.common.gamelogic.steps.*
import ru.nsk.kstatemachine.IState
import ru.nsk.kstatemachine.StateMachine



@Tags("focus")
class TestTests : FunSpec({
    test("testGameFlowTest"){
        testGameFlow(VillagerRole(), WerewolfRole()) {
            respondTo<ShowRolesState>(ConfirmRolesEvent())
            respondTo<NightStartState>(ConfirmNightStartEvent())
            respondTo<WerewolvesCall.BeforeWereWolvesVote>(WerewolvesCall.WerewolvesVoteEvent(gameDefinition.playerList[0]))
            respondTo<DayStartState>(ConfirmDayStartEvent())
            winners(WerewolvesTeam)
        }
    }
})
class GameFlowTests : WordSpec({

    "The day number" When {
        "the first night ends" should {
            "be 1" {
            }
        }
        "the second night ends" should {
            "be 2" {
                val playerList = listOf("1", "2", "3", "4")
                val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole(), WerewolfRole())
                val gameDef = createGameDefinition(this,playerList, rolesList)
                gameDef.stateMachineHolder.stateMachine.start()
            }
        }
    }

    "The win conditions" When {
        "Only villagers are present" should {
            "make the Villager win at the end of the first night" {
                    // todo
                    fail("todo")
                }
            }
        }
        "Only werewolves are present" should {
            "make the Werewolves win at the end of the first night" {
                // todo
                fail("todo")
            }
        }
        "WereWolves and Villagers are present" should {
            "allow the werewolves to win by killing all the villagers" {
                // todo
                fail("todo")
            }
            "allow the villagers to win by killing all the werewolves" {
                // todo
                fail("todo")
            }
        }


        "The mayor" When {
            "the first day starts" should {
                "be elected by the players" {
                    // todo
                    fail("todo")
                }
            }
            "the mayor dies" should {
                "be elected by the players" {
                    // todo
                    fail("todo")
                }
            }
            "the second day starts" should {
                "not be elected by the players if alive" {
                    // todo
                    fail("todo")
                }
            }
        }
    }
)
