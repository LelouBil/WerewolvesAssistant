package net.leloubil.werewolvesassistant.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.leloubil.werewolvesassistant.ui.theme.Surface
import net.leloubil.werewolvesassistant.ui.theme.Theme
import net.leloubil.werewolvesassistant.ui.theme.WerewolvesTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() = WerewolvesTheme {
    Surface(Theme.colors.background, Theme.colors.primary, modifier = Modifier.fillMaxSize()) {
        NavRoot(Modifier.fillMaxSize())
    }
}


