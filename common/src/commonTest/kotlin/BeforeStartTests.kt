import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import net.leloubil.common.gamelogic.createGameDefinition
import net.leloubil.common.gamelogic.roles.VillagerRole
import net.leloubil.common.gamelogic.roles.WerewolfRole
import ru.nsk.kstatemachine.visitors.exportToPlantUml

class BeforeStartTests : WordSpec({

    "The statemachine" should {
        "Generate a graph"{
            val playerList = listOf("1", "2", "3", "4")
            val rolesList = setOf(VillagerRole(), VillagerRole(), WerewolfRole(), VillagerRole())
            val gameDef = createGameDefinition(this, playerList, rolesList)
            println(gameDef.stateMachine.exportToPlantUml())
        }
    }

    "The creation of the game definition" should {
        "Keep the names given in parameters for each player" {
            val playerList = listOf("1", "2", "3", "4")
            val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole(), VillagerRole())
            val gameDef = createGameDefinition(this, playerList, rolesList)
            gameDef.playerList.map { it.name } shouldContainExactlyInAnyOrder playerList
        }
        "give a unique role instance to each player" {
            val playerList = listOf("1", "2", "3", "4")
            val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole(), VillagerRole())
            val gameDef = createGameDefinition(this, playerList, rolesList)
            gameDef.playerList.map { it.role } shouldContainExactlyInAnyOrder rolesList
        }
        "throw if the role count is less than to the player count" {
            val playerList = listOf("1", "2", "3", "4")
            val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole())
            shouldThrowExactly<IllegalArgumentException> {
                createGameDefinition(this, playerList, rolesList)
            }
        }
        "create an not-started state machine" {
            val playerList = listOf("1", "2", "3", "4")
            val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole(), VillagerRole())
            val gameDef = createGameDefinition(this, playerList, rolesList)
            gameDef.stateMachine.isRunning shouldBe false

        }
    }

    "The day number" When {
        "the game starts" should {
            "be 0" {
                val playerList = listOf("1", "2", "3", "4")
                val rolesList = setOf(VillagerRole(), VillagerRole(), VillagerRole(), VillagerRole())
                val gameDef = createGameDefinition(this, playerList, rolesList)
                gameDef.dayNumber shouldBe 0
            }
        }

    }
})
