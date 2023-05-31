package net.leloubil.common

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import io.github.aakira.napier.Napier
import net.leloubil.common.ui.MyApplicationTheme
import net.leloubil.common.ui.component.PrimaryButton


@Composable
fun App() {
    Napier.i { "Starting" }

    MyApplicationTheme {
        Text("Hello world!")
        PrimaryButton(onClick = {}) {
            Text("Salut")
        }
        Text("Goodnight world!")
    }
}
