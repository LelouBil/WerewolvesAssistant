package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composeunstyled.TextInput

@Composable
fun TextField(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = 1,
    colorset: ColorSet = LocalAccentColorSet.current,
    editable: Boolean = true,
    placeholder: @Composable () -> Unit = {},
) {
    com.composeunstyled.TextField(
        state = state,
        singleLine = maxLines == 1,
        maxLines = maxLines,
        fontSize = fontSize,
        modifier = modifier.background(Color.Cyan).border(2.dp,colorset.border, Theme.shapes.surface),
        editable = editable,
    ) {
        TextInput(modifier = Modifier.fillMaxHeight(),placeholder = placeholder)
    }
}
