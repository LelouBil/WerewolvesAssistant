package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.Player
import ru.nsk.kstatemachine.*


class VillagerVoteMayorEvent(override val data: Player) : DataEvent<Player>
class ChangeMayor(name: String, gameDefinition: GameDefinition) : GameStep("Change mayor $name", gameDefinition) {
    init {
        val beforeChoice = initialState("Before Mayor Choice $name")
        val mayorChanged = finalDataState<Player>("Mayor Changed $name")

        beforeChoice {
            dataTransition<VillagerVoteMayorEvent, Player>("Villagers chose new mayor") { targetState = mayorChanged }
        }
        mayorChanged {
            onEntry {
                val mayor = gameDefinition.mayor
                assert(mayor != null)
                assert(mayor!!.alive.not())
                gameDefinition.mayor = data
            }
        }

    }
}
