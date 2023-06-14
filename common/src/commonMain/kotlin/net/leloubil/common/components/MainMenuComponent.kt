package net.leloubil.common.components

import com.arkivanov.decompose.ComponentContext

interface MainMenuComponent {
    fun createGame()
}

class DefaultMainMenuComponent(componentContext: ComponentContext, private val createGame: () -> Unit) : MainMenuComponent, ComponentContext by componentContext {
    override fun createGame() = this.createGame.invoke()


}
