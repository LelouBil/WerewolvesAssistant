package net.leloubil.common.gamelogic

import io.github.aakira.napier.Napier
import ru.nsk.kstatemachine.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.jvm.isAccessible

open class DoActionableStepScope(private val actions: MutableList<() -> Unit>) {
    infix fun <T> KMutableProperty<T>.undoAssign(value: T) {
        this.getter.isAccessible = true
        val oldValue = this.getter.call()
        Napier.i { "Stored setter of ${this.name}, oldvalue: $oldValue, newValue: $value" }
        actions.add {
            Napier.i { "Undoing setter of ${this.name} to old value : $oldValue" }
            this.setter.call(oldValue)
        }
        this.setter.isAccessible = true
        this.setter.call(value)
    }
}

open class ActionableDefaultStep(
    name: String,
    private val gameDefinition: MutableGameDefinition,
) : DefaultState(name) {
    private val undoStack: ArrayDeque<List<() -> Unit>> = ArrayDeque()
    private val actionList = mutableListOf<DoActionableStepScope.() -> Unit>()

    override fun <L : IState.Listener> addListener(listener: L): L {
        throw IllegalStateException("Listeners are not supported for ActionableDataStep")
    }

    fun action(action: DoActionableStepScope.() -> Unit) {
        actionList.add(action)
    }

    override val listeners: Collection<IState.Listener>
        get() = listOf(object : IState.Listener {
            override suspend fun onEntry(transitionParams: TransitionParams<*>) {
                if (!gameDefinition.isUndoing) {
                    val undos = actionList.flatMap {
                        val curUndos: MutableList<() -> Unit> = mutableListOf()
                        DoActionableStepScope(curUndos).it()
                        curUndos
                    }
                    undoStack.addLast(undos)
                }
                else{
                    Napier.i { "Skip action bc undo" }
                }
            }
        })

    fun doUndo() {
        undoStack.removeLast().reversed().forEach { undoAction ->
            undoAction()
        }
    }

}

class DoActionableStepScopeData<D>(val data: D, actions: MutableList<() -> Unit>) : DoActionableStepScope(actions) {

}

open class ActionableDataStep<D : Any>(
    name: String,
    private val gameDefinition: MutableGameDefinition,
    dataExtractor: DataExtractor<D>
) : DefaultDataState<D>(name, dataExtractor = dataExtractor) {
    private val undoStack: ArrayDeque<List<() -> Unit>> = ArrayDeque()
    private val actionList = mutableListOf<DoActionableStepScopeData<D>.() -> Unit>()

    override fun <L : IState.Listener> addListener(listener: L): L {
        throw IllegalStateException("Listeners are not supported for ActionableDataStep")
    }

    fun action(action: DoActionableStepScopeData<D>.() -> Unit) {
        actionList.add(action)
    }

    override val listeners: Collection<IState.Listener>
        get() = listOf(object : IState.Listener {
            override suspend fun onEntry(transitionParams: TransitionParams<*>) {
                if (!gameDefinition.isUndoing) {
                    val undos = actionList.flatMap {
                        val curUndos: MutableList<() -> Unit> = mutableListOf()
                        DoActionableStepScopeData(data, curUndos).it()
                        curUndos
                    }
                    undoStack.addLast(undos)
                }else{
                    Napier.i { "Skip action bc undo" }
                }
            }
        })

    fun doUndo() {
        Napier.i { "doUndo, actions: $undoStack" }
        undoStack.removeLast().reversed().forEach { undoAction ->
            undoAction()
        }
    }
}
