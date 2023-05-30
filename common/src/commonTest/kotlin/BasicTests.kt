import io.kotest.assertions.fail
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import net.leloubil.common.gamelogic.createGameDefinition
import net.leloubil.common.gamelogic.roles.VillagerRole

class BasicTests : WordSpec({
    "The creation of the game definition" should {
        "Keep the names given in parameters for each player" {
            val playerList = listOf("1", "2", "3", "4")
            val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole(), VillagerRole())
            val gameDef = createGameDefinition(playerList, rolesList)
            gameDef.playerList.map { it.name } shouldContainExactlyInAnyOrder playerList
        }
        "give a unique role instance to each player" {
            val playerList = listOf("1", "2", "3", "4")
            val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole(), VillagerRole())
            val gameDef = createGameDefinition(playerList, rolesList)
            gameDef.playerList.map { it.role } shouldContainExactlyInAnyOrder rolesList
        }
        "throw if the role count is less than to the player count" {
            val playerList = listOf("1", "2", "3", "4")
            val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole())
            shouldThrowExactly<IllegalArgumentException> {
                createGameDefinition(playerList, rolesList)
            }
        }
        "create an not-started state machine" {
            val playerList = listOf("1", "2", "3", "4")
            val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole(), VillagerRole())
            runBlocking {
                val gameDef = createGameDefinition(playerList, rolesList)
                val holder = gameDef.buildStateMachine(this)
                holder.stateMachine.isRunning shouldBe false
            }
        }
    }

    "The day number" When {
        "the game starts" should {
            "be 0" {
                // todo
                fail("todo")
            }
        }
        "the first night ends" should {
            "be 1" {
                // todo
                fail("todo")
            }
        }
        "the second night ends" should {
            "be 2" {
                // todo
                fail("todo")
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
})
