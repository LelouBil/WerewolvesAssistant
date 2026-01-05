package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import arrow.optics.typeclasses.Index
import arrow.optics.updateCopy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.leloubil.werewolvesassistant.engine.PlayerName
import net.leloubil.werewolvesassistant.engine.Role
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam


@KoinViewModel
class ChooseRolesMenuViewModel(@InjectedParam players: List<PlayerName>) : ViewModel() {
    private val _roles: MutableStateFlow<Map<PlayerName, Role?>> = MutableStateFlow(players.associateWith { null })
    val players = _roles.asStateFlow()

    fun setRole(idx: PlayerName, role: Role) = _roles.updateCopy {
        Index.map<PlayerName, Role?>().index(idx) set role
    }

}

@Composable
fun ChooseRolesMenu(viewModel: ChooseRolesMenuViewModel) = Column {
    val players by viewModel.players.collectAsState()

    players.forEach { (name, role) ->
        Row {
            Text("$name")
            RolePicker(role) {
                viewModel.setRole(name, it)
            }
        }
    }
}

@Composable
fun RolePicker(role: Role?, setRole: (Role) -> Unit) {
    var roleMenuExpanded by remember { mutableStateOf(false) }
    val roleList = listOf(
        Role.Werewolf,
        Role.SimpleVillager
    )
    Box {
        Text(role?.toString() ?: "Choose Role", modifier = Modifier.clickable { roleMenuExpanded = true })
        DropdownMenu(roleMenuExpanded, { roleMenuExpanded = false }) {
            roleList.filter { it != role }.forEach {
                DropdownMenuItem(
                    onClick = {
                        setRole(it)
                        roleMenuExpanded = false
                    },
                    text = {
                        Text(it.toString())
                    })
            }
        }
    }
}
