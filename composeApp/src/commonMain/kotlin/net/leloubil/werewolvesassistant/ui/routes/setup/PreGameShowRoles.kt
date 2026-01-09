package net.leloubil.werewolvesassistant.ui.routes.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.composeunstyled.Text
import net.leloubil.werewolvesassistant.engine.RolesList
import net.leloubil.werewolvesassistant.ui.theme.Button
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.InjectedParam
import werewolvesassistant.composeapp.generated.resources.Res
import werewolvesassistant.composeapp.generated.resources.show_role_close
import werewolvesassistant.composeapp.generated.resources.show_role_instructions
import werewolvesassistant.composeapp.generated.resources.show_roles_player_text
import werewolvesassistant.composeapp.generated.resources.show_roles_start_button
import werewolvesassistant.composeapp.generated.resources.show_roles_title

@KoinViewModel
class PreGameShowRolesViewModel(@InjectedParam val players: RolesList, @InjectedParam val showingIndex: UInt?) :
    ViewModel() {

    val currentRole = showingIndex?.let { players[it.toInt()] }

}

@Composable
fun PreGameShowRoles(viewModel: PreGameShowRolesViewModel, nextShowIndex: () -> Unit) = Column {
    val pair = viewModel.currentRole

    if (pair == null) {
        Column {
//            Text(stringResource(Res.string.show_roles_title), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(Res.string.show_roles_title))

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
//                style = MaterialTheme.typography.titleMedium
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
