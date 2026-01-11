package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.composeunstyled.Icon
import com.composeunstyled.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.leloubil.werewolvesassistant.engine.PlayerName
import net.leloubil.werewolvesassistant.ui.theme.Button
import net.leloubil.werewolvesassistant.ui.theme.HorizontalDivider
import net.leloubil.werewolvesassistant.ui.theme.TextField
import net.leloubil.werewolvesassistant.ui.theme.Theme
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds


@KoinViewModel
class ChoosePlayersMenuViewModel : ViewModel() {

    //todo keep players from last game
    private val _playersRoles: MutableStateFlow<List<Pair<Int,TextFieldState>>> = MutableStateFlow(emptyList())
    val playersRoles = _playersRoles.asStateFlow()


    fun remove(idToRemove: Int) = _playersRoles.update {
        it.toMutableList().apply { removeAll { (id, _) -> id == idToRemove } }
    }

    fun addPlayer() = _playersRoles.update { it + (Random.nextInt() to TextFieldState()) }

}

@Composable
fun ChoosePlayersMenu(setRoles: (List<PlayerName>) -> Unit, viewModel: ChoosePlayersMenuViewModel = koinViewModel()) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(vertical = Theme.spacing.small)
    ) {
        Text("Create Game", style = Theme.typography.title)
        val players by viewModel.playersRoles.collectAsState()
        HorizontalDivider()
        LazyColumn {
            items(players.toList(), key = {(idx,_) -> idx}) { (id, textFieldState) ->

                var visible by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    visible = true
                }
                val scope = rememberCoroutineScope()
                AnimatedVisibility(visible, modifier = Modifier.fillMaxWidth(0.5f).height(50.dp).animateItem()) {

                    Row(Modifier.fillMaxWidth(0.5f).fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                        TextField(textFieldState, modifier = Modifier.fillMaxHeight().fillMaxWidth(0.8f), maxLines = 1)

                        CrossButton {
                            scope.launch {
                                visible = false
                                delay(500.milliseconds)
                                viewModel.remove(id)
                            }
                        }
                    }
                }
            }

        }

        Button(onClick = {
            viewModel.addPlayer()
        }) {
            Text("Add Player")
        }

        Button(onClick = {
            setRoles(players.map { PlayerName(it.second.text.toString()) })
        }, enabled = players.size > 3, colorSet = Theme.colors.secondary) {
            Text("Create Game")
        }


    }

@Composable
fun CrossButton(onClick: () -> Unit) {
    Button(onClick = onClick, shape = Theme.shapes.surface, padding = PaddingValues(Theme.spacing.small)) {
        Icon(Icons.Filled.Close, contentDescription = "Remove player")
    }
}
