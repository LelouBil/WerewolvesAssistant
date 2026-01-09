package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import com.composeunstyled.Text
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.leloubil.werewolvesassistant.engine.PlayerName
import net.leloubil.werewolvesassistant.ui.theme.Button
import net.leloubil.werewolvesassistant.ui.theme.HorizontalDivider
import net.leloubil.werewolvesassistant.ui.theme.TextField
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel


@KoinViewModel
class ChoosePlayersMenuViewModel : ViewModel() {

    //todo keep players from last game
    private val _playersRoles: MutableStateFlow<List<TextFieldState>> = MutableStateFlow(emptyList())
    val playersRoles = _playersRoles.asStateFlow()


    fun remove(idx: Int) = _playersRoles.update {
        it.toMutableList().apply { removeAt(idx) }
    }

    fun addPlayer() = _playersRoles.update { it + TextFieldState() }

}

@Composable
fun ChoosePlayersMenu(setRoles: (List<PlayerName>) -> Unit, viewModel: ChoosePlayersMenuViewModel = koinViewModel()) =
    Column {
        Text("Create Game")
        val players by viewModel.playersRoles.collectAsState()
        HorizontalDivider()
        players.forEachIndexed { idx, textFieldState ->
            TextField(textFieldState)

            HorizontalDivider()

            CrossButton {
                viewModel.remove(idx)
            }
        }

        Button(onClick = {
            viewModel.addPlayer()
        }) {
            Text("Add Player")
        }

        Button(onClick = {
            setRoles(players.map { PlayerName(it.text.toString()) })
        }) {
            Text("Create Game")
        }


    }

@Composable
fun CrossButton(onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text("X")
    }
}
