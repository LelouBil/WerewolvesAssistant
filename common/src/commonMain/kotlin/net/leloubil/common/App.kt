@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package net.leloubil.common


import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import io.github.aakira.napier.Napier


@Composable
fun App() {
    Napier.i { "Starting" }
    Button(onClick = {
    }) {
        Text("Salut")
    }

}
