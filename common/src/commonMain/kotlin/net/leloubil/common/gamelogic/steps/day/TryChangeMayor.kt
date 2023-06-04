package net.leloubil.common.gamelogic.steps.day

import io.github.aakira.napier.Napier
import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.steps.SelfContinueDataStep
import net.leloubil.common.gamelogic.steps.SelfContinueDefaultStep
import net.leloubil.common.gamelogic.steps.selfContinuation
import ru.nsk.kstatemachine.*


open class TryChangeMayor(name: String, private val gameDefinition: GameDefinition) :
    DefaultState("Change mayor $name") {

    inner class CheckCurrentMayorStep : SelfContinueDefaultStep("Check current mayor $name", gameDefinition)
    inner class ChooseNewMayorStep : DefaultState("Choose new mayor $name") {
        inner class ChooseNewMayorEvent(override val data: Player) : DataEvent<Player>
    }

    inner class ShowNewMayorStep :
        SelfContinueDataStep<Player>("Show new mayor $name", gameDefinition, dataExtractor = defaultDataExtractor()) {
        init {
            onEntry {
                Napier.i { "New mayor is $data" }
                this@TryChangeMayor.gameDefinition.mayor = data
            }
        }
    }

    init {
        val checkCurrentMayor = addInitialState(CheckCurrentMayorStep())
        val chooseNewMayor = addState(ChooseNewMayorStep())
        val showNewMayor = addState(ShowNewMayorStep())
        val finishedTryChangeMayor = finalState("Finished Try Change Mayor $name")

        checkCurrentMayor {
            selfContinuation {
                guard =
                    { this@TryChangeMayor.gameDefinition.mayor != null && this@TryChangeMayor.gameDefinition.mayor!!.alive }
                targetState = finishedTryChangeMayor
            }
            selfContinuation {
                guard =
                    { this@TryChangeMayor.gameDefinition.mayor == null || this@TryChangeMayor.gameDefinition.mayor!!.alive.not() }
                targetState = chooseNewMayor
            }
        }

        chooseNewMayor {
            dataTransition<ChooseNewMayorStep.ChooseNewMayorEvent, Player>("Choose new mayor $name") {
                targetState = showNewMayor
            }
        }

        showNewMayor {
            selfContinuation {
                targetState = finishedTryChangeMayor
            }
        }
    }

}
