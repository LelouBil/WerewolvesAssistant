package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.composeunstyled.TextInput

@Composable
fun TextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    editable: Boolean = true,
    placeholder: @Composable () -> Unit = {},
) {
    com.composeunstyled.TextField(
        state = state,
        modifier = modifier,
        editable = editable,
    ) {
        TextInput(placeholder = placeholder)
    }
}