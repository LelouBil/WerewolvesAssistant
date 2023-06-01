package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.Player
import ru.nsk.kstatemachine.*


class VillagerVoteMayorEvent(override val data: Player) : DataEvent<Player>
class ChangeMayor(name: String, gameDefinition: GameDefinition) : GameStep("Change mayor $name", gameDefinition) {
    private var mayorHistory: ArrayDeque<Player> = ArrayDeque()

    init {
        val beforeChoice = initialState("Before Mayor Choice $name")
        val mayorChanged = finalDataState<Player>("Mayor Changed $name")

        beforeChoice {
            dataTransition<VillagerVoteMayorEvent, Player>("A new mayor is chosen $name") { targetState = mayorChanged }
            // undo
            onEntry { transitionParams ->
                if (transitionParams.unwrappedEvent is UndoEvent) {
                    gameDefinition.mayor = this@ChangeMayor.mayorHistory.removeFirst()
                }
            }
        }
        mayorChanged {
            onEntry {
                val mayor = gameDefinition.mayor
                assert(mayor != null)
                assert(mayor!!.alive.not())
                this@ChangeMayor.mayorHistory.addFirst(mayor)
                gameDefinition.mayor = data
            }
        }

    }
}
