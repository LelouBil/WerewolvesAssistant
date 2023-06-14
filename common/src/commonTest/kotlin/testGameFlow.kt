import io.github.aakira.napier.Napier
import io.kotest.assertions.withClue
import io.kotest.core.test.TestScope
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockkConstructor
import net.leloubil.common.gamelogic.*
import net.leloubil.common.gamelogic.roles.BaseRole
import net.leloubil.common.gamelogic.steps.SelfContinueGameStep
import net.leloubil.common.gamelogic.steps.win.ProcessKillsCheckWin
import ru.nsk.kstatemachine.*
import kotlin.reflect.KClass

sealed class ResponseToStep{
    class SendEvent(val stateType: KClass<*>, val eventToSend: (IState.() -> Event), val stepSourcecodeLocation: String) : ResponseToStep()
    class Undo(val until : KClass<*>, val stepSourcecodeLocation: String) : ResponseToStep()
}

class GameStepsScope(val gameDefinition: MutableGameDefinition) {
    val steps = ArrayDeque<ResponseToStep>()
    var winners: Set<Team>? = null
    val players = gameDefinition.playerList

    inline fun <reified T : IState> When(noinline eventToSend: T.() -> Event) {
        withClue("There shouldn't be any calls to respondTo in the game flow test after the game is finished") {
            winners shouldBe null
        }


        steps.addLast(
            ResponseToStep.SendEvent(
                T::class, eventToSend as (IState.() -> Event), Thread.currentThread().stackTrace[1].toString()
            )
        )
    }

    fun Undo(previousInteractStep : KClass<*>) {
        steps.addLast(
            ResponseToStep.Undo(
                previousInteractStep, Thread.currentThread().stackTrace[1].toString()
            )
        )
    }

    inline fun confirmKills(alive: List<BaseRole> = listOf(), dead: List<BaseRole> = listOf()) {
        When<ProcessKillsCheckWin.ProcessKillsStepPart> {
            players.shouldNowBe(alive, dead)
            ConfirmKillsEvent()
        }
    }

    fun List<Player>.shouldNowBe(alive: List<BaseRole> = listOf(), dead: List<BaseRole> = listOf()) {

        val livingAssert = alive.map { role -> role::class }
        val deadAssert = dead.map { role -> role::class }

        withClue("There should be ${livingAssert.size + deadAssert.size} players") {
            this shouldHaveSize livingAssert.size + deadAssert.size
        }
        withClue("There should be ${livingAssert.size} living players") {
            this.living().map { it.role::class } shouldContainExactlyInAnyOrder livingAssert
        }
        withClue("There should be ${deadAssert.size} dead players") {
            this.dead().map { it.role::class } shouldContainExactlyInAnyOrder deadAssert
        }
    }

    fun winners(vararg winners: Team) {
        this.winners = winners.toSet()
    }

}
//todo pour les callback des state pour l'UI, utiliser plus ou moins ce qu'il y a en dessous et le refactoriser pour que les deux utilisent le meme truc
// -> la partie qui renvoie que une state si y'a un event a renvoyer.

public suspend fun TestScope.testGameFlow(vararg roleList: BaseRole, gameSteps: GameStepsScope.() -> Unit) : MutableGameDefinition {
    Napier.i { "Starting test" }
    val players = (1..roleList.size).map { "Player $it" }
    mockkConstructor(MutableGameDefinition::class)
    val gameDef = createGameDefinition(this, players, roleList.toSet())
    val gameStepsScope = GameStepsScope(gameDef as MutableGameDefinition)
    gameStepsScope.gameSteps()
    val stateEventHistory = ArrayDeque<Pair<IState, Event>>()
    withClue("Winners should be set at the end of the game flow test") {
        gameStepsScope.winners shouldNotBe null
    }
    var reachedEnd = false
    var lastState: IState? = null
    var undoingUntil: KClass<*>? = null
    gameDef.stateMachine.addListener(object : StateMachine.Listener {
        override suspend fun onStateFinished(state: IState, transitionParams: TransitionParams<*>) {
            if (state is StateMachine) {
                reachedEnd = true
            }
        }

        override suspend fun onTransitionTriggered(transitionParams: TransitionParams<*>) {
            val targetStateName = if(transitionParams.direction.targetState != null){
                transitionParams.direction.targetState!!::class.simpleName
            } else{
                "null"
            }
            Napier.i { " ${transitionParams.transition.sourceState::class.simpleName} --transition--> $targetStateName" }
        }

        override suspend fun onStateExit(state: IState, transitionParams: TransitionParams<*>) {
//            Napier.i { "State ${state.name} exited" }
        }

        override suspend fun onStateEntry(state: IState, transitionParams: TransitionParams<*>) {
            Napier.i { "${if (state is ActionableDefaultStep && state !is SelfContinueGameStep) "!!! " else ""}State ${state::class.simpleName} entered" }
            if(undoingUntil != null){
                if(state::class == undoingUntil){
                    Napier.i { "<-! Undo finished" }
                    undoingUntil = null
                }else{
                    Napier.i { "<--- Undoing until ${undoingUntil!!.simpleName}" }
                    return
                }
            }
            lastState = state
//            Napier.i { "State ${state.name} entered" }
            if (state !is SelfContinueGameStep && state !is IFinalState && state !is StateMachine && state.states.isEmpty()) {
                withClue("Missing step for ${state::class.simpleName}") {
                    gameStepsScope.steps shouldHaveAtLeastSize 1
                }
                //                Napier.i { "${response.stateType.simpleName} == ${state::class.simpleName}" }

                when(val response = gameStepsScope.steps.removeFirst()){
                    is ResponseToStep.SendEvent -> {
                        withClue("Next step is ${state::class.simpleName} but test expected ${response.stateType.simpleName}\nat ${response.stepSourcecodeLocation}\n") {
                            state::class shouldBeEqual response.stateType
                        }
                        val eventBuilder = response.eventToSend
                        val event = eventBuilder(state)
                        Napier.i { "--> ${event::class.simpleName}" }
                        stateEventHistory.addLast(state to event)
                        gameDef.stateMachine.processEvent(event)
                    }

                    is ResponseToStep.Undo -> {
                        undoingUntil = response.until
                        Napier.i { "<--- Starting undo until ${response.until.simpleName}"}
                        gameDef.stateMachine.undo()
                    }
                }



            }

        }

        override suspend fun onTransitionComplete(
            transitionParams: TransitionParams<*>, activeStates: Set<IState>
        ) {

        }
    })
    gameDef.stateMachine.start()
    withClue("Game not finished at the end of the test, Remaining state : ${lastState?.toString()}, Players: ${gameDef.playerList}") {
        reachedEnd shouldBe true
    }
    gameStepsScope.steps shouldHaveSize 0
    val expectedWinners: Set<Team> = gameStepsScope.winners!!
    val winners = gameDef.winners
    winners.shouldContainExactly(expectedWinners)
    return gameDef
}
