package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


@Composable
fun Surface(colorSet: ColorSet, accent: ColorSet, modifier: Modifier = Modifier, content: @Composable () -> Unit) =
    ProvideContentColorSet(colorSet, accentColor = accent) {
        Box(it.then(modifier)) {
            content()
        }
    }
