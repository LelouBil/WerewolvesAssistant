package net.leloubil.common.components

import com.arkivanov.decompose.ComponentContext

interface CreateGameComponent {
}

class DefaultCreateGameComponent(componentContext: ComponentContext) : CreateGameComponent,
    ComponentContext by componentContext {

}
