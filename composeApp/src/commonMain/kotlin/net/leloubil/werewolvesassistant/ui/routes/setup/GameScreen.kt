package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import net.leloubil.werewolvesassistant.engine.Game
import net.leloubil.werewolvesassistant.engine.RolesList
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam


@KoinViewModel
class GameScreenViewModel(@InjectedParam players: RolesList) : ViewModel() {
    //todo save/load game persistently
    val game = Game(players)

}

@Composable
fun GameScreen(viewModel: GameScreenViewModel) {
    Text(viewModel.game.toString())
}
