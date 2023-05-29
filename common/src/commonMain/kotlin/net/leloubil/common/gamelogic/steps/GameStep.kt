package net.leloubil.common.gamelogic.steps

import net.leloubil.common.gamelogic.GameDefinition
import ru.nsk.kstatemachine.DefaultState

open class GameStep(name: String, protected val gameDefinition: GameDefinition) : DefaultState(name) {
}