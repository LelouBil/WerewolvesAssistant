package net.leloubil.common

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import net.leloubil.common.ui.MyApplicationTheme
import net.leloubil.common.ui.component.PrimaryButton
import net.leloubil.common.ui.component.SecondaryButton
import net.leloubil.common.ui.component.Title


@Composable
fun App() {
    Napier.i { "Starting" }

    MyApplicationTheme {
        Title("Loups-Garous de Thiercelieux")
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton("Lancer une partie") {
            Napier.i { "Start Game" }
        }
        PrimaryButton("Continuer", disabled = true) {
            Napier.i { "Continue" }
        }
        SecondaryButton("Historique") {
            Napier.i { "History" }
        }
    }
}
