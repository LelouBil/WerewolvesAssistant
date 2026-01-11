package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.composeunstyled.ProvideTextStyle
import com.composeunstyled.Text
import com.composeunstyled.UnstyledButton
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = Theme.shapes.button,
    padding: PaddingValues = PaddingValues(Theme.spacing.medium),
    colorSet: ColorSet = LocalAccentColorSet.current,
    content: @Composable () -> Unit,
) = ProvideContentColorSet(colorSet) {

    val alphaOver by animateFloatAsState(if (enabled) 1f else 0.5f)

//    val alphaOver = 0.5f
//    val bgOver = if(enabled) it else Modifier.background(colorSet.background.withAlpha(alpha = alphaOver))
//    val contentOver = if(enabled) LocalContentColor.current else LocalContentColor.current.copy(alpha = alphaOver)
//    val borderOver = if(enabled) colorSet.border else colorSet.border.withAlpha(alpha = alphaOver)
//    ProvideContentColor(contentOver) {
    ProvideTextStyle(Theme.typography.buttonTitle) {
        UnstyledButton(
            onClick,
            modifier = Modifier.graphicsLayer {
                alpha = alphaOver
            }.padding(padding).clip(shape)
                .then(it)
                .border(Theme.spacing.small, colorSet.border, shape)
                .then(modifier),
            contentPadding = PaddingValues(Theme.spacing.medium),
            enabled = enabled
        ) {
            content()
        }
    }
//    }
}

@Preview
@Composable
fun ButtonPreview() {
    Button(onClick = {}) {
        Text("Button")
    }
}
