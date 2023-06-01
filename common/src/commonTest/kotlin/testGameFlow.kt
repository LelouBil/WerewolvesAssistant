import io.github.aakira.napier.Napier
import io.kotest.assertions.withClue
import io.kotest.core.test.TestScope
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.leloubil.common.gamelogic.GameDefinition
import net.leloubil.common.gamelogic.Player
import net.leloubil.common.gamelogic.Team
import net.leloubil.common.gamelogic.createGameDefinition
import net.leloubil.common.gamelogic.roles.BaseRole
import net.leloubil.common.gamelogic.steps.GameStep
import net.leloubil.common.gamelogic.steps.SelfContinueGameStep
import ru.nsk.kstatemachine.Event
import ru.nsk.kstatemachine.IState
import ru.nsk.kstatemachine.StateMachine
import ru.nsk.kstatemachine.TransitionParams
import kotlin.reflect.KClass

class ResponseToStep(val stateType: KClass<*>, val eventToSend: (IState.() -> Event))

class GameStepsScope(val gameDefinition: GameDefinition) {
    val steps = ArrayDeque<ResponseToStep>()
    var winners: Set<Team>? = null
    val players = gameDefinition.playerList

    inline fun <reified T : GameStep> respondTo(noinline eventToSend: T.() -> Event) {
        withClue("There shouldn't be any calls to respondTo in the game flow test after the game is finished") {
            winners shouldBe null
        }
        steps.addLast(ResponseToStep(T::class, eventToSend as (IState.() -> Event)))
    }

    inline fun <reified T : GameStep> respondTo(eventToSend: Event) {
        val evtLambda: T.() -> Event = { eventToSend }
        respondTo<T>(evtLambda)
    }

    fun winners(vararg winners: Team) {
        this.winners = winners.toSet()
    }

    fun List<Player>.living() = filter { it.alive }

    fun List<Player>.dead() = filter { !it.alive }

    inline fun <reified T : BaseRole> List<Player>.role() = filter { it.role is T }

}

public suspend fun TestScope.testGameFlow(vararg roleList: BaseRole, gameSteps: GameStepsScope.() -> Unit) {
    Napier.i { "Starting test" }
    val players = (1..roleList.size).map { "Player $it" }
    val gameDef = createGameDefinition(this, players, roleList.toSet())
    val stateMachineHolder = gameDef.stateMachineHolder
    val gameStepsScope = GameStepsScope(gameDef)
    gameStepsScope.gameSteps()
    val stateEventHistory = ArrayDeque<Pair<GameStep, Event>>()
    withClue("Winners should be set at the end of the game flow test") {
        gameStepsScope.winners shouldNotBe null
    }
    var reachedEnd = false
    stateMachineHolder.stateMachine.addListener(object : StateMachine.Listener {


        override suspend fun onStateFinished(state: IState, transitionParams: TransitionParams<*>) {
            if (state is StateMachine) {
                reachedEnd = true
            }
        }

        override suspend fun onStateExit(state: IState, transitionParams: TransitionParams<*>) {
            Napier.i { "State ${state.name} exited" }
        }

        override suspend fun onStateEntry(state: IState, transitionParams: TransitionParams<*>) {
            Napier.i { "State ${state.name} entered" }
            if (state is GameStep && state !is SelfContinueGameStep && state !is StateMachine && state.states.isEmpty()) {
                gameStepsScope.steps shouldHaveAtLeastSize 1
                val response = gameStepsScope.steps.removeFirst()
                Napier.i { "Sending event to state ${state::class.simpleName}" }
                state::class shouldBe response.stateType
                val eventBuilder = response.eventToSend
                val event = eventBuilder(state)
                stateEventHistory.addLast(state to event)
                stateMachineHolder.stateMachine.processEvent(event)
            }

        }

        override suspend fun onTransitionComplete(
            transitionParams: TransitionParams<*>,
            activeStates: Set<IState>
        ) {
            Napier.i { "Transition complete to states ${activeStates.map { activeStates::class.simpleName }}" }
        }
    })
    stateMachineHolder.stateMachine.start()
    reachedEnd shouldBe true
    gameStepsScope.steps shouldHaveSize 0
    val expectedWinners: Set<Team> = gameStepsScope.winners!!
    val winners = gameDef.winners
    winners.shouldContainExactly(expectedWinners)
}

