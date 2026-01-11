package net.leloubil.werewolvesassistant.ui.routes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composeunstyled.Text
import net.leloubil.werewolvesassistant.ui.theme.Button
import net.leloubil.werewolvesassistant.ui.theme.Theme
import org.jetbrains.compose.resources.stringResource
import werewolvesassistant.composeapp.generated.resources.Res
import werewolvesassistant.composeapp.generated.resources.create_game_button

@Composable
fun MainMenu(createGame: () -> Unit) = Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    Button(
        onClick = createGame
    ) {
        Text(stringResource(Res.string.create_game_button), style = Theme.typography.buttonTitle)
    }
}
