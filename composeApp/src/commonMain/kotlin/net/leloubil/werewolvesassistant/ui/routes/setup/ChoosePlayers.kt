package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import arrow.optics.typeclasses.Index
import arrow.optics.updateCopy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.leloubil.werewolvesassistant.engine.PlayerName
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel


@KoinViewModel
class ChoosePlayersMenuViewModel : ViewModel() {

    //todo keep players from last game
    private val _playersRoles: MutableStateFlow<List<PlayerName>> = MutableStateFlow(emptyList())
    val playersRoles = _playersRoles.asStateFlow()

    fun setName(idx: Int, value: PlayerName) = _playersRoles.updateCopy {
        Index.list<PlayerName>().index(idx) set value
    }

    fun remove(idx: Int) = _playersRoles.update {
        it.toMutableList().apply { removeAt(idx) }
    }

    fun addPlayer() = _playersRoles.update { it + PlayerName("") }

}

@Composable
fun ChoosePlayersMenu(setRoles: (List<PlayerName>) -> Unit, viewModel: ChoosePlayersMenuViewModel = koinViewModel()) =
    Column {
        Text("Create Game")
        val players by viewModel.playersRoles.collectAsState()
        HorizontalDivider()
        players.forEachIndexed { idx, playerSlot ->
            TextField(
                playerSlot.name,
                onValueChange = {
                    viewModel.setName(idx, PlayerName(it))
                })
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
            setRoles(players)
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
