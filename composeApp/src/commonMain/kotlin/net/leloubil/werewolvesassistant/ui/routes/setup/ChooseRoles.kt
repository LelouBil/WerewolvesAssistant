package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.leloubil.werewolvesassistant.engine.PlayerName
import net.leloubil.werewolvesassistant.engine.Role
import net.leloubil.werewolvesassistant.engine.RolesList
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import werewolvesassistant.composeapp.generated.resources.*


@KoinViewModel
class ChooseRolesMenuViewModel(@InjectedParam val players: List<PlayerName>) : ViewModel() {

//    private val _roles: MutableStateFlow<Map<PlayerName, Role?>> = MutableStateFlow(players.associateWith { null })
//    val players = _roles.asStateFlow()

//    fun setRole(idx: PlayerName, role: Role) = _roles.updateCopy {
//        Index.map<PlayerName, Role?>().index(idx) set role

    private val _counts: MutableStateFlow<Map<Role, UInt>> = MutableStateFlow(emptyMap())
    val counts = _counts.asStateFlow()

    private val _assignments = MutableStateFlow<List<Pair<PlayerName, Role>>?>(null)
    val assignments = _assignments.asStateFlow()

    fun assignPlayersRandomly() {
        val totalRoles = counts.value.values.sum()
        if (totalRoles != players.size.toUInt()) return

        val allRoles = counts.value.flatMap { (role, count) -> List(count.toInt()) { role } }.shuffled()
        _assignments.value = players.mapIndexed { idx, playerName -> playerName to allRoles[idx] }
    }

    fun setRoleCount(role: Role, count: UInt) {
        _assignments.value = null
        _counts.update {
            it + (role to count)
        }
    }
}

@Composable
fun ChooseRolesMenu(viewModel: ChooseRolesMenuViewModel, preGame: (RolesList) -> Unit) =
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val counts by viewModel.counts.collectAsState()

        Text(stringResource(Res.string.select_roles_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(Res.string.select_roles_subtitle), style = MaterialTheme.typography.titleSmall)

        Text("${counts.values.sum()} / ${viewModel.players.size}")

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val roles = listOf(
                Role.Werewolf,
                Role.SimpleVillager
            )
            roles.forEach {
                RolePicker(it, counts[it] ?: 0u, true) { role, count -> viewModel.setRoleCount(role, count) }
            }
        }

        HorizontalDivider()

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val roles = listOf(
                Role.Hunter,
                Role.Seer,
                Role.Guard,
                Role.Witch,
                Role.Cupid,
                Role.WhiteWolf
            )
            roles.forEach {
                RolePicker(it, counts[it] ?: 0u) { role, count -> viewModel.setRoleCount(role, count) }
            }
        }

        Button(
            enabled = counts.values.sum() == viewModel.players.size.toUInt(),
            onClick = {
                viewModel.assignPlayersRandomly()
            }
        ) {
            Text(stringResource(Res.string.select_roles_randomize))
        }

        val assignments by viewModel.assignments.collectAsState()
        if (assignments != null)
            Text(assignments.toString())

        Button(onClick = {
            assignments?.let { preGame(it) }
        }, enabled = assignments != null) {
            Text(stringResource(Res.string.select_roles_continue))
        }
    }

@Composable
private fun RolePicker(role: Role, count: UInt, multiple: Boolean = false, onCountUpdate: (Role, UInt) -> Unit) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(pluralStringResource(role.name, count.toInt()))

        if (multiple) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = {
                    onCountUpdate(role, if (count == 0u) 0u else count - 1u)
                }) {
                    Text("-")
                }
                Text(count.toString())
                Button(onClick = { onCountUpdate(role, count + 1u) }) {
                    Text("+")
                }
            }
        } else {
            Button(onClick = { onCountUpdate(role, if (count == 0u) 1u else 0u) }) {
                Icon(Icons.Default.Check, contentDescription = null)
                Text(stringResource(if (count == 0u) Res.string.select_roles_add else Res.string.select_roles_remove))
            }
        }
    }
}

/*@Composable
fun RolePicker(role: Role?, setRole: (Role) -> Unit) {
    var roleMenuExpanded by remember { mutableStateOf(false) }
    val roleList = listOf(
        Role.Werewolf,
        Role.SimpleVillager,
        Role.Hunter,
        Role.Seer
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
}*/
