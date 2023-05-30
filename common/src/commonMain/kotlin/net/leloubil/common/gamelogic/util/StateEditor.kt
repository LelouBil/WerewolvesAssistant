package net.leloubil.common.gamelogic.util

import ru.nsk.kstatemachine.State

class StateEditor<T : State> {

    private val applyList = mutableListOf<T>()
    operator fun invoke(editor: T.() -> Unit) {
        applyList.forEach(editor)
    }

    operator fun invoke(value: T) = applyList.add(value)

}
