package net.leloubil.common.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth(.6f), border = BorderStroke(4.dp, Color.Black)) {
        content()
    }
}