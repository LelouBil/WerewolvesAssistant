package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.composeunstyled.Text
import com.composeunstyled.UnstyledButton
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colorSet: ColorSet = LocalAccentColorSet.current,
    content: @Composable () -> Unit
) = ProvideContentColorSet(colorSet) {
    UnstyledButton(
        onClick,
        modifier = it.then(modifier),
        contentPadding = PaddingValues(Theme.spacing.small),
        shape = RectangleShape,
        borderColor = colorSet.content,
        borderWidth = 1.dp,
        enabled = enabled
    ) {
        content()
    }
}

@Preview
@Composable
fun ButtonPreview() {
    Button(onClick = {}) {
        Text("Button")
    }
}
