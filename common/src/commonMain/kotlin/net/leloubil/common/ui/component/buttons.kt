package net.leloubil.common.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.leloubil.common.ui.theme.NightBlue

private val commonModifier = Modifier.fillMaxWidth(.6f)
private val commonBorder = BorderStroke(3.dp, Color.Black)

@Composable
fun PrimaryButton(text: String, disabled: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !disabled,
        modifier = with(commonModifier) { if (disabled) alpha(.6f) else this }.then(modifier),
        border = commonBorder
    ) {
        Text(text)
    }
}

@Composable
fun SecondaryButton(text: String, disabled: Boolean = false, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !disabled,
        modifier = with(commonModifier) { if (disabled) alpha(.6f) else this }.then(modifier),
        colors = ButtonDefaults.buttonColors(backgroundColor = NightBlue, disabledBackgroundColor = NightBlue),
        border = commonBorder
    ) {
        Text(text)
    }
}

