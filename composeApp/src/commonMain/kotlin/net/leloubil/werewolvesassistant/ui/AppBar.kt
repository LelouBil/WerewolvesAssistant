package net.leloubil.werewolvesassistant.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.leloubil.werewolvesassistant.ui.theme.Surface
import net.leloubil.werewolvesassistant.ui.theme.Theme


@Composable
fun AppBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Surface(Theme.colors.primary, Theme.colors.secondary, modifier = modifier) {
        Row(Modifier.fillMaxWidth().then(modifier), verticalAlignment = Alignment.CenterVertically) {
            content()
        }
    }
}
