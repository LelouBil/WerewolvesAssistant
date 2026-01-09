package net.leloubil.werewolvesassistant.ui.theme

import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.composeunstyled.Icon
import com.composeunstyled.UnstyledCheckbox

@Composable
fun Checkbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    colorSet: ColorSet = LocalAccentColorSet.current,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) = ProvideContentColorSet(colorSet) {
    UnstyledCheckbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = it.requiredSize(20.dp).then(modifier),
    ) {
        Icon(Icons.Default.Check, contentDescription = "Checked")
    }
}

