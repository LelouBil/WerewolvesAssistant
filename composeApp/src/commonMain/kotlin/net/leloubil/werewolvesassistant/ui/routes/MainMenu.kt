package net.leloubil.werewolvesassistant.ui.routes

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.composeunstyled.Text
import net.leloubil.werewolvesassistant.ui.theme.Button
import org.jetbrains.compose.resources.stringResource
import werewolvesassistant.composeapp.generated.resources.Res
import werewolvesassistant.composeapp.generated.resources.create_game_button

@Composable
fun MainMenu(createGame: () -> Unit) {
    Column {
        Button(
            onClick = createGame
        ) {
            Text(stringResource(Res.string.create_game_button))
        }
    }
}
