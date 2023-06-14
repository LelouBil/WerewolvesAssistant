package net.leloubil.common.components

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.Parcelable
import com.arkivanov.essenty.parcelable.Parcelize

interface RootComponent{
    val stack: Value<ChildStack<*, Child>>

    // It's possible to pop multiple screens at a time on iOS
    fun onBackClicked(toIndex: Int)

    // Defines all possible child components
    sealed class Child {
        class MainMenuChild(val component: MainMenuComponent) : Child()
        class CreateGameChild(val component: CreateGameComponent) : Child()
    }
}
class DefaultRootComponent(componentContext: ComponentContext) : RootComponent, ComponentContext by componentContext {


    private val navigation = StackNavigation<Config>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            initialConfiguration = Config.MainMenu, // The initial child component is List
            handleBackButton = true, // Automatically pop from the stack on back button presses
            childFactory = ::child,
        )

    private fun child(config: Config, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            is Config.MainMenu -> RootComponent.Child.MainMenuChild(mainMenu(componentContext))
            is Config.CreateGame -> RootComponent.Child.CreateGameChild(createGame(componentContext))
        }

    private fun mainMenu(componentContext: ComponentContext): MainMenuComponent =
        DefaultMainMenuComponent(
            componentContext = componentContext,
            createGame = { navigation.push(Config.CreateGame) }
        )

    private fun createGame(componentContext: ComponentContext): CreateGameComponent =
        DefaultCreateGameComponent(componentContext)

    override fun onBackClicked(toIndex: Int) {
        navigation.popTo(index = toIndex)
    }

    @Parcelize // The `kotlin-parcelize` plugin must be applied if you are targeting Android
    private sealed interface Config : Parcelable {
        object MainMenu : Config
        object CreateGame : Config
    }
}
