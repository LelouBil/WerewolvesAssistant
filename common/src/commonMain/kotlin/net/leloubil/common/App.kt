package net.leloubil.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.Children
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.jetbrains.stack.animation.stackAnimation
import io.github.aakira.napier.Napier
import net.leloubil.common.components.MainMenuComponent
import net.leloubil.common.components.RootComponent
import net.leloubil.common.ui.MyApplicationTheme
import net.leloubil.common.ui.components.PrimaryButton
import net.leloubil.common.ui.components.SecondaryButton
import net.leloubil.common.ui.components.Title

@Composable
fun App(rootComponent: RootComponent) {
    MyApplicationTheme {
        Children(
            stack = rootComponent.stack,
            animation = stackAnimation(fade() + scale())
        ) {
            when (val child = it.instance) {
                is RootComponent.Child.MainMenuChild -> MainMenu(child.component)
                is RootComponent.Child.CreateGameChild -> CreateGame()
            }
        }
    }
}

@Composable
fun CreateGame() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Title("Créer une partie")
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton("Ajouter un joueur") {
            Napier.i { "Create" }
        }
    }
}

@Composable
fun MainMenu(mainMenuComponent: MainMenuComponent) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        Title("Loups-Garous de Thiercelieux")
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton("Lancer une partie") {
            Napier.i { "Start Game" }
            mainMenuComponent.createGame()
        }
        PrimaryButton("Continuer", disabled = true) {
            Napier.i { "Continue" }
        }
        SecondaryButton("Historique") {
            Napier.i { "History" }
        }
    }
}
