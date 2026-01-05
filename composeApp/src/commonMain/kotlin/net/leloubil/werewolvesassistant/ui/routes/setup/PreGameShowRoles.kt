package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import net.leloubil.werewolvesassistant.engine.RolesList
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import werewolvesassistant.composeapp.generated.resources.*

@KoinViewModel
class PreGameShowRolesViewModel(@InjectedParam val players: RolesList, @InjectedParam val showingIndex: UInt?) : ViewModel() {

    val currentRole = showingIndex?.let { players[it.toInt()] }

}

@Composable
fun PreGameShowRoles(viewModel: PreGameShowRolesViewModel, nextShowIndex: () -> Unit) = Column {
    val pair = viewModel.currentRole

    if (pair == null) {
        Column {
            Text(stringResource(Res.string.show_roles_title), style = MaterialTheme.typography.titleLarge)
            Button(onClick = {
                nextShowIndex()
            }) {
                Text(stringResource(Res.string.show_roles_start_button))
            }
        }
    } else {
        val (player, role) = pair

        Column {
            var revealed by remember { mutableStateOf(false) }
            Text(
                stringResource(Res.string.show_roles_player_text, player.name),
                style = MaterialTheme.typography.titleMedium
            )
            if (!revealed) {
                Button(onClick = {
                    revealed = true
                }) {
                    Text(stringResource(Res.string.show_role_instructions))
                }
            } else {
                Text(pluralStringResource(role.name, 1))
                Button(onClick = {
                    nextShowIndex()
                }) {
                    Text(stringResource(Res.string.show_role_close))
                }
            }
        }
    }
}
